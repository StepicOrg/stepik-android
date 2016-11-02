package org.stepic.droid.util

import java.util.concurrent.atomic.AtomicBoolean

operator fun AtomicBoolean.not() = !this.get()

