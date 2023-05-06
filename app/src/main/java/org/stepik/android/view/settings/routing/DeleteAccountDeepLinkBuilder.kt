package org.stepik.android.view.settings.routing

import org.stepic.droid.configuration.EndpointResolver
import javax.inject.Inject

class DeleteAccountDeepLinkBuilder
@Inject
constructor(
    private val endpointResolver: EndpointResolver
) {
    fun buildDeleteAccountUrl(): String =
        "${endpointResolver.getBaseUrl()}/$PATH_DELETE_ACCOUNT"

    companion object {
        private const val PATH_DELETE_ACCOUNT = "users/delete-account/"
    }
}