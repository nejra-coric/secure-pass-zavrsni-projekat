package com.nejracoric.securepassandroid.native

import android.util.Log

object SecurePassNative {

    private const val TAG = "SecurePassNative"

    @Volatile
    private var librariesLoaded = false

    private var loadError: String? = null

    private val SWIFT_RUNTIME_LIBRARIES = listOf(
        "c++_shared",
        "BlocksRuntime",
        "swiftCore",
        "swiftAndroid",
        "swift_RegexParser",
        "swift_StringProcessing",
    )

    init {
        librariesLoaded = loadNativeLibraries()
    }

    private fun loadNativeLibraries(): Boolean {
        for (library in SWIFT_RUNTIME_LIBRARIES) {
            if (!loadLibrarySafe(library, required = false)) {
                Log.w(TAG, "Swift runtime library unavailable: $library")
            }
        }

        return loadLibrarySafe("securepass_engine", required = true)
    }

    private fun loadLibrarySafe(name: String, required: Boolean): Boolean {
        return try {
            System.loadLibrary(name)
            Log.d(TAG, "Loaded library: lib${name}.so")
            true
        } catch (error: UnsatisfiedLinkError) {
            val message = "Failed to load lib${name}.so: ${error.message}"
            if (required) {
                Log.e(TAG, message, error)
                loadError = message
            } else {
                Log.w(TAG, message)
            }
            !required
        } catch (error: Exception) {
            val message = "Unexpected error loading lib${name}.so: ${error.message}"
            Log.e(TAG, message, error)
            if (required) {
                loadError = message
            }
            !required
        }
    }

    fun isAvailable(): Boolean = librariesLoaded

    fun loadErrorMessage(): String? = loadError

    private external fun nativeGeneratePassword(
        length: Int,
        includeSpecial: Int,
        buffer: ByteArray,
        bufferSize: Int,
    ): Int

    external fun nativeCalculateEntropy(password: ByteArray): Double

    fun generatePassword(length: Int, includeSpecialChars: Boolean): Result<String> {
        if (!librariesLoaded) {
            return Result.failure(
                IllegalStateException(loadError ?: "Native libraries are not loaded")
            )
        }

        if (length <= 0) {
            return Result.failure(IllegalArgumentException("Length must be positive"))
        }

        return try {
            val buffer = ByteArray(length + 1)
            val status = nativeGeneratePassword(
                length = length,
                includeSpecial = if (includeSpecialChars) 1 else 0,
                buffer = buffer,
                bufferSize = buffer.size,
            )

            when (status) {
                0 -> Result.success(buffer.decodeToString().trimEnd('\u0000'))
                -2 -> Result.failure(IllegalStateException("Native buffer too small"))
                else -> Result.failure(IllegalArgumentException("Native generation failed: $status"))
            }
        } catch (error: UnsatisfiedLinkError) {
            val message = "Native call failed (UnsatisfiedLinkError): ${error.message}"
            Log.e(TAG, message, error)
            Result.failure(IllegalStateException(message, error))
        } catch (error: Exception) {
            val message = "Native call failed: ${error.message}"
            Log.e(TAG, message, error)
            Result.failure(IllegalStateException(message, error))
        }
    }

    fun calculateEntropy(password: String): Double {
        if (!librariesLoaded || password.isEmpty()) return 0.0

        return try {
            val bytes = (password + "\u0000").toByteArray(Charsets.UTF_8)
            nativeCalculateEntropy(bytes)
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "calculateEntropy failed (UnsatisfiedLinkError): ${error.message}", error)
            0.0
        } catch (error: Exception) {
            Log.e(TAG, "calculateEntropy failed: ${error.message}", error)
            0.0
        }
    }
}
