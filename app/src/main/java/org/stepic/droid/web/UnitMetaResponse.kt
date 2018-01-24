package org.stepic.droid.web

import org.stepic.droid.model.Meta
import org.stepic.droid.model.Unit

class UnitMetaResponse(meta: Meta, val units: List<Unit>?) : MetaResponseBase(meta)