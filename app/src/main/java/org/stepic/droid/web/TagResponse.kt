package org.stepic.droid.web

import org.stepik.android.model.learning.Tag
import org.stepik.android.model.Meta

class TagResponse(
        meta: Meta,
        val tags: List<Tag>
) : MetaResponseBase(meta)
