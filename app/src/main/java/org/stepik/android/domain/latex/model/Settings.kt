package org.stepik.android.domain.latex.model

data class Settings(val allowUniversalAccessFromFileURLs: Boolean) {
    companion object {
        val DEFAULT_SETTINGS = Settings(allowUniversalAccessFromFileURLs = false)
    }
    operator fun plus(other: Settings): Settings =
        Settings(allowUniversalAccessFromFileURLs = this.allowUniversalAccessFromFileURLs || other.allowUniversalAccessFromFileURLs)
}