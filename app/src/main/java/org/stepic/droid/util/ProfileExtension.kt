package org.stepic.droid.util

import org.stepic.droid.model.Profile

fun Profile.getFirstAndLastName(): String
        = this.first_name + " " + this.last_name