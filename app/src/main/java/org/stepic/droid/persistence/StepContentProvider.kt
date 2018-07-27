package org.stepic.droid.persistence

interface StepContentProvider {
    fun getDownloadableContentFromStep(): List<String>
}