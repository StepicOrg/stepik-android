package org.stepik.android.model.learning.certificates

import com.google.gson.annotations.SerializedName
import java.util.Date

class Certificate(
    val id: Long? = null,
    val user: Long? = null,
    val course: Long? = null,
    
    @SerializedName("issue_date")
    val issueDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null,

    val grade: String? = null,
    val type: CertificateType? = null,
    val url: String? = null
)