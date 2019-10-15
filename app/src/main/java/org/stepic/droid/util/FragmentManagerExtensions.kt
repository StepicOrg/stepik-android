package org.stepic.droid.util

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * Run [body] in a [FragmentTransaction] which is automatically committed if it completes without
 * exception.
 *
 * The transaction will be completed by calling [FragmentTransaction.commit] unless [allowStateLoss]
 * is set to `true` in which case [FragmentTransaction.commitAllowingStateLoss] will be used.
 */
inline fun FragmentManager.commit(
    allowStateLoss: Boolean = false,
    body: FragmentTransaction.() -> Unit
) {
    val transaction = beginTransaction()
    transaction.body()
    if (allowStateLoss) {
        transaction.commitAllowingStateLoss()
    } else {
        transaction.commit()
    }
}
/**
 * Run [body] in a [FragmentTransaction] which is automatically committed if it completes without
 * exception.
 *
 * The transaction will be completed by calling [FragmentTransaction.commitNow] unless
 * [allowStateLoss] is set to `true` in which case [FragmentTransaction.commitNowAllowingStateLoss]
 * will be used.
 */
inline fun FragmentManager.commitNow(
    allowStateLoss: Boolean = false,
    body: FragmentTransaction.() -> Unit
) {
    val transaction = beginTransaction()
    transaction.body()
    if (allowStateLoss) {
        transaction.commitNowAllowingStateLoss()
    } else {
        transaction.commitNow()
    }
}
/**
 * Run [body] in a [FragmentTransaction] which is automatically committed if it completes without
 * exception.
 *
 * One of four commit functions will be used based on the values of `now` and `allowStateLoss`:
 *
 *     |  now  |  allowStateLoss  | Method                         |
 *     | ----- | ---------------- | ------------------------------ |
 *     | false | false            |  commit()                      |
 *     | false | true             |  commitAllowingStateLoss()     |
 *     | true  | false            |  commitNow()                   |
 *     | true  | true             |  commitNowAllowingStateLoss()  |
 */
@Deprecated("Use commit { .. } or commitNow { .. } extensions")
inline fun FragmentManager.transaction(
    now: Boolean = false,
    allowStateLoss: Boolean = false,
    body: FragmentTransaction.() -> Unit
) {
    val transaction = beginTransaction()
    transaction.body()
    if (now) {
        if (allowStateLoss) {
            transaction.commitNowAllowingStateLoss()
        } else {
            transaction.commitNow()
        }
    } else {
        if (allowStateLoss) {
            transaction.commitAllowingStateLoss()
        } else {
            transaction.commit()
        }
    }
}