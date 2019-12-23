package org.stepik.android.domain.billing.model

import org.junit.Assert
import org.junit.Test
import org.solovyev.android.checkout.Sku
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SkuSerializableWrapperTest {
    @Test
    fun skuSerializableWrapperSerializationTest() {
        val byteOutputStream = ByteArrayOutputStream()
        val outputStream = ObjectOutputStream(byteOutputStream)

        val sku = Sku(
            "prod", "code", "price",
            Sku.Price(0, "USD"),
            "title", "description", "introductoryPrice",
            Sku.Price(0, "USD"),
            "subscriptionPeriod", "freeTrialPeriod", "introductoryPricePeriod", 0)

        outputStream.writeObject(
            SkuSerializableWrapper(
                sku
            )
        )
        outputStream.close()

        val bytes = byteOutputStream.toByteArray()

        val byteInputStream = ByteArrayInputStream(bytes)
        val inputStream = ObjectInputStream(byteInputStream)

        val skuWrapper = inputStream.readObject() as SkuSerializableWrapper

        Assert.assertEquals(sku.toJson(), skuWrapper.sku.toJson())
    }
}