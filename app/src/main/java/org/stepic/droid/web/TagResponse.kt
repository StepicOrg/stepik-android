package org.stepic.droid.web

import org.stepik.android.model.Meta
import org.stepik.android.model.Tag

class TagResponse(
        meta: Meta,
        val tags: List<Tag>
) : MetaResponseBase(meta)
