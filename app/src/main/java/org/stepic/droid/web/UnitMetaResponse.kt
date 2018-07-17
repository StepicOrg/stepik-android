package org.stepic.droid.web

import org.stepic.droid.model.Unit
import org.stepik.android.model.Meta

class UnitMetaResponse(meta: Meta, val units: List<Unit>?) : MetaResponseBase(meta)