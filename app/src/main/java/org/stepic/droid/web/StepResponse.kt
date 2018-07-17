package org.stepic.droid.web

import org.stepic.droid.model.Step
import org.stepik.android.model.Meta

class StepResponse(meta: Meta, val steps: List<Step>?) : MetaResponseBase(meta)
