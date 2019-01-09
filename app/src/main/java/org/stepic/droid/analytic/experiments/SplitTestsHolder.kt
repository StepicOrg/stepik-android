package org.stepic.droid.analytic.experiments

import javax.inject.Inject

class SplitTestsHolder
@Inject
constructor(
    splitTests: Set<@JvmSuppressWildcards SplitTest<*>>
)