package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepik.android.model.learning.Tag

class TagResponse(
        meta: Meta,
        val tags: List<Tag>
) : MetaResponseBase(meta)
