package org.stepik.android.presentation.debug

import com.android.billingclient.api.Purchase

interface InAppPurchasesFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()
        data class Content(val purchases: List<Purchase>) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchPurchasesSuccess(val purchases: List<Purchase>) : Message()
        object FetchPurchasesFailure : Message()

        data class PurchaseClickedMessage(val purchase: Purchase) : Message()
        object ConsumeAllMessage : Message()

        data class ConsumeSuccess(val purchase: Purchase) : Message()
        object ConsumeAllSuccess : Message()
        object ConsumeFailure : Message()
    }

    sealed class Action {
        object FetchPurchases : Action()
        data class ConsumePurchase(val purchase: Purchase) : Action()
        data class ConsumeAllPurchases(val purchases: List<Purchase>) : Action()
        sealed class ViewAction : Action() {
            object ShowLoading : ViewAction()
            object ShowConsumeSuccess : ViewAction()
            object ShowConsumeFailure : ViewAction()
        }
    }
}