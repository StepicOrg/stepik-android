package org.stepic.droid.persistence.model

sealed class Task(
        val configuration: Configuration
) {
    class CacheUnit
}

class Configuration // video quality, target path, etc..
