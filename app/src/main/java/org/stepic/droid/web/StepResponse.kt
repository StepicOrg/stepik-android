package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepic.droid.model.Step

class StepResponse(meta: Meta, val steps: List<Step>?) : MetaResponseBase(meta) {
}
