package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.EmailAddress
import org.stepic.droid.model.Meta

data class EmailAddressResponse(
        var meta: Meta?,
        @SerializedName("email-addresses")
        var emailAddresses: List<EmailAddress?>?
)