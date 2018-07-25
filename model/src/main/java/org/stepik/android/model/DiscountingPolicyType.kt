package org.stepik.android.model

import com.google.gson.annotations.SerializedName

enum class DiscountingPolicyType {
    @SerializedName("no_discount")
    NoDiscount,
    @SerializedName("inverse")
    Inverse,
    @SerializedName("first_one")
    FirstOne,
    @SerializedName("first_three")
    FirstThree;

    fun numberOfTries(): Int =
            when (this) {
                NoDiscount, Inverse -> Int.MAX_VALUE
                FirstOne -> 1
                FirstThree -> 3
            }
}
