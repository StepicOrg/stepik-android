package org.stepic.droid.util

import org.stepic.droid.model.User


fun User.getFirstAndLastName(): String
        = this.first_name + " " + this.last_name