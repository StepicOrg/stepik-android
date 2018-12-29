package org.stepik.android.view.routing.deeplink

import org.json.JSONObject

interface BranchDeepLinkParser {
    /**
     * @return BranchRoute or null if failed to parse
     */
    fun parseBranchDeepLink(params: JSONObject): BranchRoute?
}