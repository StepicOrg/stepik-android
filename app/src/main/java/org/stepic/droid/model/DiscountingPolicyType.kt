package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

enum class DiscountingPolicyType {
    @SerializedName("no_discount")
    noDiscount,
    @SerializedName("inverse")
    inverse,
    @SerializedName("first_one")
    firstOne,
    @SerializedName("first_three")
    firstThree;

    fun numberOfTries(): Int =
            when (this) {
                noDiscount, inverse -> Int.MAX_VALUE
                firstOne -> 1
                firstThree -> 3
            }
}
