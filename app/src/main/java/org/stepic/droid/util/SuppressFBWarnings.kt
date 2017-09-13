package org.stepic.droid.util


@Retention(AnnotationRetention.BINARY)
annotation class SuppressFBWarnings(
        /**
         * The set of FindBugs warnings that are to be suppressed in
         * annotated element. The value can be a bug category, kind or pattern.
         */
        vararg val value: String = arrayOf(),
        /**
         * Optional documentation of the reason why the warning is suppressed
         */
        val justification: String = "")