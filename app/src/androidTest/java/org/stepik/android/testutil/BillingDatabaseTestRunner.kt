package org.stepik.android.testutil

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class BillingDatabaseTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, Application::class.java.name, context)
}
