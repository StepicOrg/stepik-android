package org.stepic.droid.model

data class EmailAddress(
        var id: Long?,
        var user: Long?,
        var email: String?,
        var is_verified: Boolean?,
        var is_primary: Boolean?
)