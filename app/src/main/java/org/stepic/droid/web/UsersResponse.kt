package org.stepic.droid.web

import org.stepik.android.model.user.User
import org.stepik.android.model.Meta

class UsersResponse(
        meta: Meta,
        val users: List<User>?
) : MetaResponseBase(meta)
