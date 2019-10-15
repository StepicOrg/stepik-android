package org.stepik.android.remote.email_address.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.user.EmailAddress
import org.stepik.android.remote.base.model.MetaResponse

class EmailAddressResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("email-addresses")
    val emailAddresses: List<EmailAddress>
) : MetaResponse