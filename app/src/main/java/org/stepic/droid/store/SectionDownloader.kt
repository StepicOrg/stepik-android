package org.stepic.droid.store

interface SectionDownloader {

    fun downloadSection(sectionId: Long)

    fun cancelSectionLoading(sectionId: Long)

    fun deleteWholeSection(sectionId: Long)
}
