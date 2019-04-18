package org.stepik.android.remote.email_address.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.EmailAddress

class EmailAddressRequest(
    @SerializedName("emailAddress")
    val emailAddress: EmailAddress
)