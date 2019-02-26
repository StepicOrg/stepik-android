package org.stepik.android.domain.billing.model

import org.solovyev.android.checkout.Sku
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class SkuSerializableWrapper(
    _sku: Sku
) : Serializable {
    @Transient
    var sku: Sku = _sku
        private set

    private fun writeObject(stream: ObjectOutputStream) {
        stream.writeObject(sku.id.product)
        stream.writeObject(sku.id.code)

        stream.writeObject(sku.price)

        stream.writeLong(sku.detailedPrice.amount)
        stream.writeObject(sku.detailedPrice.currency)

        stream.writeObject(sku.title)
        stream.writeObject(sku.description)
        stream.writeObject(sku.introductoryPrice)

        stream.writeLong(sku.detailedIntroductoryPrice.amount)
        stream.writeObject(sku.detailedIntroductoryPrice.currency)

        stream.writeObject(sku.subscriptionPeriod)
        stream.writeObject(sku.freeTrialPeriod)
        stream.writeObject(sku.introductoryPricePeriod)
        stream.writeInt(sku.introductoryPriceCycles)
    }

    private fun readObject(stream: ObjectInputStream) {
        sku = Sku(
            stream.readObject() as String, // product
            stream.readObject() as String, // code

            stream.readObject() as String, // price

            Sku.Price(
                stream.readLong(),
                stream.readObject() as String
            ),

            stream.readObject() as String, // title
            stream.readObject() as String, // description
            stream.readObject() as String, // introductoryPrice

            Sku.Price(
                stream.readLong(),
                stream.readObject() as String
            ),

            stream.readObject() as String, // subscriptionPeriod
            stream.readObject() as String, // freeTrialPeriod
            stream.readObject() as String, // introductoryPricePeriod

            stream.readInt() // introductoryPriceCycles
        )
    }
}