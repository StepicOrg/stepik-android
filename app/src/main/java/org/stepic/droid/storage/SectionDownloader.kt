package org.stepic.droid.storage

interface SectionDownloader {

    fun downloadSection(sectionId: Long)

    fun cancelSectionLoading(sectionId: Long)

    fun deleteWholeSection(sectionId: Long)
}
