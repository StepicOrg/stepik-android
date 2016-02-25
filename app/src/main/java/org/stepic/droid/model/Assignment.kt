package org.stepic.droid.model

class Assignment : IProgressable {
    var id: Long = 0
    var step: Long = 0
    var unit: Long = 0
    var progress: String? = null
    var create_date: String? = null
    var update_date: String? = null

    override fun getProgressId() = progress
}
