package org.stepik.android.domain.course_purchase.model

import android.os.Parcel
import android.os.Parcelable
import com.android.billingclient.api.Purchase
import kotlinx.android.parcel.Parcelize

sealed class PurchaseResult : Parcelable {
    @Parcelize
    object Unavailable : PurchaseResult()
    @Parcelize
    data class Empty(val obfuscatedParams: CoursePurchaseObfuscatedParams) : PurchaseResult()

    data class Result(val obfuscatedParams: CoursePurchaseObfuscatedParams, val purchase: Purchase) : PurchaseResult() {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(obfuscatedParams.obfuscatedAccountId)
            parcel.writeString(obfuscatedParams.obfuscatedProfileId)
            parcel.writeString(purchase.originalJson)
            parcel.writeString(purchase.signature)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Result> {
            override fun createFromParcel(parcel: Parcel): Result =
                Result(
                    CoursePurchaseObfuscatedParams(
                        parcel.readString()!!,
                        parcel.readString()!!
                    ),
                    Purchase(
                        parcel.readString()!!,
                        parcel.readString()!!
                    )
                )

            override fun newArray(size: Int): Array<Result?> =
                arrayOfNulls(size)
        }
    }
}