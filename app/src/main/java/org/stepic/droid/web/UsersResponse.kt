package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepic.droid.model.User

class UsersResponse(
        meta: Meta,
        val users: List<User>?
) : MetaResponseBase(meta)
