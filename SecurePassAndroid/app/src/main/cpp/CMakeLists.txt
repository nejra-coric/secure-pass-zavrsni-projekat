cmake_minimum_required(VERSION 3.22.1)
project(securepass_engine)

set(SECUREPASS_ENGINE_ROOT "${CMAKE_SOURCE_DIR}/../../../../../SecurePassEngine")
set(SECUREPASS_SWIFT_LIB_DIR "${SECUREPASS_ENGINE_ROOT}/build/android/${ANDROID_ABI}")
set(SWIFT_RUNTIME_DIR "${SECUREPASS_SWIFT_LIB_DIR}/runtime")

set(SECUREPASS_ENGINE_SOURCES securepass_jni_bridge.c)
set(SECUREPASS_ENGINE_LIBS)
set(SECUREPASS_LINK_WHOLE_ARCHIVE OFF)

find_library(log-lib log)

if(EXISTS "${SECUREPASS_SWIFT_LIB_DIR}/libSecurePassEngine.a")
    message(STATUS "Linking Swift static engine for ${ANDROID_ABI}")

    add_library(SecurePassEngine STATIC IMPORTED GLOBAL)
    set_target_properties(SecurePassEngine PROPERTIES
            IMPORTED_LOCATION "${SECUREPASS_SWIFT_LIB_DIR}/libSecurePassEngine.a")
    list(APPEND SECUREPASS_ENGINE_LIBS SecurePassEngine)
    set(SECUREPASS_LINK_WHOLE_ARCHIVE ON)

    if(EXISTS "${SWIFT_RUNTIME_DIR}")
        set(SWIFT_RUNTIME_LIBS
                swiftCore
                swiftAndroid
                swift_RegexParser
                swift_StringProcessing
                BlocksRuntime
                c++_shared)

        foreach(_swift_lib ${SWIFT_RUNTIME_LIBS})
            set(_swift_lib_path "${SWIFT_RUNTIME_DIR}/lib${_swift_lib}.so")
            if(EXISTS "${_swift_lib_path}")
                add_library(swift_runtime_${_swift_lib} SHARED IMPORTED GLOBAL)
                set_target_properties(swift_runtime_${_swift_lib} PROPERTIES
                        IMPORTED_LOCATION "${_swift_lib_path}")
                list(APPEND SECUREPASS_ENGINE_LIBS swift_runtime_${_swift_lib})
                message(STATUS "  Swift runtime: ${_swift_lib_path}")
            else()
                message(WARNING "Missing Swift runtime library: ${_swift_lib_path}")
            endif()
        endforeach()
    else()
        message(WARNING "Swift runtime directory not found: ${SWIFT_RUNTIME_DIR}. Run ../build_swift_android.sh")
    endif()

elseif(EXISTS "${SECUREPASS_SWIFT_LIB_DIR}/libSecurePassEngine.so")
    message(STATUS "Linking Swift shared engine for ${ANDROID_ABI}")
    add_library(SecurePassEngine SHARED IMPORTED GLOBAL)
    set_target_properties(SecurePassEngine PROPERTIES
            IMPORTED_LOCATION "${SECUREPASS_SWIFT_LIB_DIR}/libSecurePassEngine.so")
    list(APPEND SECUREPASS_ENGINE_LIBS SecurePassEngine)

else()
    message(WARNING "Swift engine not found for ${ANDROID_ABI}; using C fallback implementation.")
    list(APPEND SECUREPASS_ENGINE_SOURCES securepass_engine.c)
endif()

add_library(securepass_engine SHARED ${SECUREPASS_ENGINE_SOURCES})

target_include_directories(securepass_engine PRIVATE
        "${SECUREPASS_ENGINE_ROOT}/include")

if(SECUREPASS_LINK_WHOLE_ARCHIVE)
    list(REMOVE_ITEM SECUREPASS_ENGINE_LIBS SecurePassEngine)
    target_link_libraries(securepass_engine PRIVATE
            -Wl,--whole-archive
            SecurePassEngine
            -Wl,--no-whole-archive
            ${SECUREPASS_ENGINE_LIBS}
            ${log-lib}
            m)
else()
    target_link_libraries(securepass_engine PRIVATE
            ${SECUREPASS_ENGINE_LIBS}
            ${log-lib}
            m)
endif()
