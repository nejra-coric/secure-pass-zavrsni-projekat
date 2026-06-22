#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENGINE_DIR="${PROJECT_ROOT}/SecurePassEngine"
ANDROID_ABI="arm64-v8a"
SWIFT_SDK_ID="${SWIFT_ANDROID_SDK_ID:-swift-6.2-RELEASE-android-24-0.1}"
SWIFT_SDK_TRIPLE="aarch64-unknown-linux-android24"
BUILD_CONFIG="${BUILD_CONFIG:-release}"
OUTPUT_DIR="${ENGINE_DIR}/build/android/${ANDROID_ABI}"
JNILIBS_DIR="${PROJECT_ROOT}/SecurePassAndroid/app/src/main/jniLibs/${ANDROID_ABI}"
RUNTIME_DIR="${OUTPUT_DIR}/runtime"

SWIFT_ANDROID_SDK_URL="${SWIFT_ANDROID_SDK_URL:-https://github.com/finagolfin/swift-android-sdk/releases/download/6.2/swift-6.2-RELEASE-android-24-0.1.artifactbundle.tar.gz}"
SWIFT_ANDROID_SDK_CHECKSUM="${SWIFT_ANDROID_SDK_CHECKSUM:-c26ebfd4e32c0ca1beabcc45729b62042da57ee76d7d043f63f2235da90dc491}"

SWIFT_RUNTIME_LIBS=(
    libswiftCore.so
    libswiftAndroid.so
    libswift_RegexParser.so
    libswift_StringProcessing.so
    libBlocksRuntime.so
)

log() { printf '[build_swift_android] %s\n' "$*"; }
die() { printf '[build_swift_android] ERROR: %s\n' "$*" >&2; exit 1; }

usage() {
    cat <<'EOF'
Usage: ./build_swift_android.sh [--install-sdk]

Environment variables:
  ANDROID_NDK_HOME   Path to Android NDK (auto-detected from ANDROID_HOME if unset)
  BUILD_CONFIG       Swift build configuration: release (default) or debug
  SWIFT_TOOLCHAIN    Optional path to swift toolchain bin directory

Options:
  --install-sdk      Install Swift Android SDK bundle via `swift sdk install`
EOF
}

install_swift_sdk() {
    if swift sdk list 2>/dev/null | grep -q "${SWIFT_SDK_ID}"; then
        log "Swift Android SDK already installed (${SWIFT_SDK_TRIPLE})"
        return 0
    fi

    log "Installing Swift Android SDK..."
    swift sdk install "${SWIFT_ANDROID_SDK_URL}" --checksum "${SWIFT_ANDROID_SDK_CHECKSUM}"
}

resolve_ndk() {
    if [[ -n "${ANDROID_NDK_HOME:-}" && -d "${ANDROID_NDK_HOME}" ]]; then
        export ANDROID_NDK_HOME
        return 0
    fi

    if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}/ndk" ]]; then
        local latest_ndk
        latest_ndk="$(find "${ANDROID_HOME}/ndk" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)"
        if [[ -n "${latest_ndk}" ]]; then
            export ANDROID_NDK_HOME="${latest_ndk}"
            log "Using ANDROID_NDK_HOME=${ANDROID_NDK_HOME}"
            return 0
        fi
    fi

    local mac_ndk="${HOME}/Library/Android/sdk/ndk"
    if [[ -d "${mac_ndk}" ]]; then
        local latest_ndk
        latest_ndk="$(find "${mac_ndk}" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)"
        if [[ -n "${latest_ndk}" ]]; then
            export ANDROID_NDK_HOME="${latest_ndk}"
            log "Using ANDROID_NDK_HOME=${ANDROID_NDK_HOME}"
            return 0
        fi
    fi

    die "ANDROID_NDK_HOME not found. Set it or install NDK via Android Studio."
}

find_swift_sdk_bundle_root() {
  local sdk_root="${SWIFT_ANDROID_SDK_ROOT:-}"
  if [[ -n "${sdk_root}" && -d "${sdk_root}" ]]; then
    printf '%s\n' "${sdk_root}"
    return 0
  fi

  local candidates=(
    "${HOME}/Library/org.swift.swiftpm/swift-sdks"
    "${HOME}/.swiftpm/swift-sdks"
  )

  local base candidate bundle
  for base in "${candidates[@]}"; do
    [[ -d "${base}" ]] || continue
    for bundle in "${base}"/*android*.artifactbundle; do
      [[ -d "${bundle}" ]] || continue
      printf '%s\n' "${bundle}"
      return 0
    done
  done

  return 1
}

find_swift_runtime_source_dir() {
    local bundle_root="$1"
    local search_roots=(
        "${bundle_root}"
        "${bundle_root}/swift-6.2-release-android-24-sdk"
        "${bundle_root}/swift-android"
    )

    local root candidate
    for root in "${search_roots[@]}"; do
        [[ -d "${root}" ]] || continue
        while IFS= read -r candidate; do
            if [[ -f "${candidate}/libswiftCore.so" ]]; then
                printf '%s\n' "${candidate}"
                return 0
            fi
        done < <(find "${root}" -type d \( \
            -path '*/usr/lib/aarch64-linux-android' -o \
            -path '*/usr/lib/swift-aarch64/android' -o \
            -path '*/usr/lib/swift/aarch64-linux-android' \
            \) 2>/dev/null)
    done

    return 1
}

copy_swift_runtime_libs() {
    local runtime_source="$1"
    mkdir -p "${RUNTIME_DIR}"
    rm -rf "${JNILIBS_DIR}"
    mkdir -p "${JNILIBS_DIR}"

    local lib required_missing=0
    for lib in "${SWIFT_RUNTIME_LIBS[@]}"; do
        if [[ -f "${runtime_source}/${lib}" ]]; then
            cp -f "${runtime_source}/${lib}" "${RUNTIME_DIR}/${lib}"
            cp -f "${runtime_source}/${lib}" "${JNILIBS_DIR}/${lib}"
            log "Copied ${lib}"
        else
            log "WARNING: missing ${lib} in ${runtime_source}"
            required_missing=1
        fi
    done

    local cxx_shared="${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/$(uname | tr '[:upper:]' '[:lower:]')-x86_64/sysroot/usr/lib/aarch64-linux-android/libc++_shared.so"
    if [[ -f "${cxx_shared}" ]]; then
        cp -f "${cxx_shared}" "${RUNTIME_DIR}/libc++_shared.so"
        cp -f "${cxx_shared}" "${JNILIBS_DIR}/libc++_shared.so"
        log "Copied libc++_shared.so from NDK"
    else
        log "WARNING: libc++_shared.so not found in NDK"
        required_missing=1
    fi

    if [[ "${required_missing}" -ne 0 ]]; then
        die "One or more required Swift runtime libraries are missing. Check your Swift Android SDK installation."
    fi
}

build_swift_engine() {
    [[ -d "${ENGINE_DIR}" ]] || die "Missing SecurePassEngine directory"

    unset ANDROID_NDK_ROOT || true

    local -a swift_build_cmd=(swift build -c "${BUILD_CONFIG}" --swift-sdk "${SWIFT_SDK_TRIPLE}")
    if [[ -n "${SWIFT_TOOLCHAIN:-}" ]]; then
        swift_build_cmd+=(--toolchain "${SWIFT_TOOLCHAIN}")
    fi

    log "Building SecurePassEngine for ${SWIFT_SDK_TRIPLE} (${BUILD_CONFIG})..."
    (cd "${ENGINE_DIR}" && "${swift_build_cmd[@]}")

    local swift_build_dir="${ENGINE_DIR}/.build/${SWIFT_SDK_TRIPLE}/${BUILD_CONFIG}"
    local static_lib="${swift_build_dir}/libSecurePassEngine.a"
    local object_file="${swift_build_dir}/SecurePassEngine.build/SecurePassEngine.swift.o"

    mkdir -p "${OUTPUT_DIR}"

    if [[ -f "${static_lib}" ]]; then
        cp -f "${static_lib}" "${OUTPUT_DIR}/libSecurePassEngine.a"
    elif [[ -f "${object_file}" ]]; then
        local ndk_prebuilt="${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt"
        local llvm_ar
        llvm_ar="$(find "${ndk_prebuilt}" -name llvm-ar -type f 2>/dev/null | head -n 1)"
        [[ -n "${llvm_ar}" ]] || die "llvm-ar not found in Android NDK"

        "${llvm_ar}" rcs "${OUTPUT_DIR}/libSecurePassEngine.a" "${object_file}"
        log "Archived ${object_file} into ${OUTPUT_DIR}/libSecurePassEngine.a"
    else
        die "Neither static library nor object file found under ${swift_build_dir}"
    fi

    log "Installed ${OUTPUT_DIR}/libSecurePassEngine.a"
}

main() {
    local install_sdk=0
    if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
        usage
        exit 0
    fi
    if [[ "${1:-}" == "--install-sdk" ]]; then
        install_sdk=1
        shift
    fi

    command -v swift >/dev/null 2>&1 || die "swift not found in PATH"
    resolve_ndk

    if ! swift sdk list 2>/dev/null | grep -q "${SWIFT_SDK_ID}"; then
        if [[ "${install_sdk}" -eq 1 ]]; then
            install_swift_sdk
        else
            die "Swift Android SDK (${SWIFT_SDK_ID}) not installed. Re-run with --install-sdk"
        fi
    fi

    local bundle_root runtime_source
    bundle_root="$(find_swift_sdk_bundle_root)" || die "Could not locate Swift Android SDK bundle under ~/Library/org.swift.swiftpm/swift-sdks"
    runtime_source="$(find_swift_runtime_source_dir "${bundle_root}")" || die "Could not locate Swift runtime libraries for aarch64 in SDK bundle"

    log "Swift SDK bundle: ${bundle_root}"
    log "Swift runtime source: ${runtime_source}"

    build_swift_engine
    copy_swift_runtime_libs "${runtime_source}"

    log "Done."
    log "  Engine:  ${OUTPUT_DIR}/libSecurePassEngine.a"
    log "  Runtime: ${RUNTIME_DIR}"
    log "  jniLibs: ${JNILIBS_DIR}"
    log "Next: rebuild Android app (./gradlew :app:assembleDebug)"
}

main "$@"
