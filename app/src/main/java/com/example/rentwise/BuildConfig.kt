// Temporary BuildConfig stub to unblock compilation on machines without JDK 11+.
// IMPORTANT: This is a short-term workaround. Remove this file once you build with Gradle (after installing JDK 11+)
// and the real BuildConfig is generated. Also rotate any API key that was committed here.

package com.example.rentwise

object BuildConfig {
    // Mirror the field added to app/build.gradle.kts so code depending on it compiles.
    // Replace the value with your real key (or keep it empty) and DO NOT commit a real secret to source control in production.
    const val OPENROUTER_API_KEY: String = "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"

    // Match the generated BuildConfig.DEBUG; set true for debug builds
    const val DEBUG: Boolean = true
}

