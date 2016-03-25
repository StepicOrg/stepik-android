package org.stepic.droid.base

import android.os.Bundle
import android.support.v4.app.Fragment

import org.stepic.droid.R

abstract class SingleFragmentActivity : FragmentActivityBase() {
    protected abstract fun createFragment(): Fragment?
    open fun getLayoutResId() = R.layout.activity_fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResId())

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragmentContainer)

        if (fragment == null) {
            fragment = createFragment()
            fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
        }

    }

}