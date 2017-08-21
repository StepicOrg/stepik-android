package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.CertificatesFragment

/**
 * Temp activity for certificates
 */
class CertificatesActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment = CertificatesFragment.newInstance()
}
