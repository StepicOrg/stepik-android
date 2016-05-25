package org.stepic.droid.core

import com.squareup.otto.Bus

import org.stepic.droid.base.MainApplication
import org.stepic.droid.web.IApi

import javax.inject.Inject

class CommentManager {

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var api: IApi

    @Inject
    constructor() {
        MainApplication.component().inject(this)
    }


}
