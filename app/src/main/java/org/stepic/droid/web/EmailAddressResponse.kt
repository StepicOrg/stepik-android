package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.user.EmailAddress
import org.stepic.droid.model.Meta

class EmailAddressResponse(
        var meta: Meta?,
        @SerializedName("email-addresses")
        var emailAddresses: List<EmailAddress?>?
)