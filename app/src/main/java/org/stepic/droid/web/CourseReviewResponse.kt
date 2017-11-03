package org.stepic.droid.web

import org.stepic.droid.model.Meta

class CourseReviewResponse(
        meta: Meta,
        val average: Double
) : MetaResponseBase(meta)
