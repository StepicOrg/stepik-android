package org.stepic.droid.model;

import com.google.gson.annotations.SerializedName;

public enum DiscountingPolicyType {
    @SerializedName("no_discount")
    noDiscount,
    @SerializedName("inverse")
    inverse,
    @SerializedName("first_one")
    firstOne,
    @SerializedName("first_three")
    firstThree
}
