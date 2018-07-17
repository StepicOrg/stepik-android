package org.stepic.droid.web

import org.stepik.android.model.structure.Unit
import org.stepik.android.model.Meta

class UnitMetaResponse(meta: Meta, val units: List<Unit>?) : MetaResponseBase(meta)