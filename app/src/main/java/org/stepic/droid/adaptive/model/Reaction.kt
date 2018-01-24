package org.stepic.droid.adaptive.model

enum class Reaction(val value: Int) {
    SOLVED(2), INTERESTING(1), MAYBE_LATER(0), NEVER_AGAIN(-1)
}