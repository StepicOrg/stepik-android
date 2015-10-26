package org.stepic.droid.view.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import org.jetbrains.anko.support.v4.alert
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.operations.DatabaseManager
import org.stepic.droid.util.cleanDirectory


import javax.inject.Inject

class ClearCacheDialogFragment : DialogFragment() {

    @Inject
    lateinit internal var userPreferences: UserPreferences

    @Inject
    lateinit internal var mDatabaseManager: DatabaseManager


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        MainApplication.component().inject(this)

        return alert {
            title(R.string.title_clear_cache_dialog)
            message(R.string.clear_cache_dialog_message)
            positiveButton(R.string.yes) {
                userPreferences.downloadFolder.cleanDirectory()
                mDatabaseManager.dropDatabase()
            }
            negativeButton (R.string.no) { }
        }.builder.create()
    }
}
