package org.stepik.android.domain.step.interactor

import android.content.Context
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Actions
import com.google.firebase.appindexing.builders.Indexables
import org.stepic.droid.configuration.EndpointResolver
import org.stepic.droid.util.StringUtil
import org.stepik.android.model.Lesson
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import javax.inject.Inject

class StepIndexingInteractor
@Inject
constructor(
    private val context: Context,
    private val firebaseAppIndex: FirebaseAppIndex,
    private val firebaseUserActions: FirebaseUserActions,
    private val endpointResolver: EndpointResolver
) {
    private var action: Action? = null

    fun startIndexing(unit: Unit?, lesson: Lesson, step: Step) {
        firebaseAppIndex.update(newIndexable(unit, lesson, step))

        action = newAction(unit, lesson, step)
        action?.let(firebaseUserActions::start)
    }

    fun endIndexing() {
        action?.let(firebaseUserActions::end)
        action = null
    }

    private fun newAction(unit: Unit?, lesson: Lesson, step: Step): Action =
        Actions.newView(
            StringUtil.getTitleForStep(context, lesson, step.position),
            StringUtil.getUriForStep(endpointResolver.getBaseUrl(), lesson, unit, step)
        )

    private fun newIndexable(unit: Unit?, lesson: Lesson, step: Step): Indexable =
        Indexables.newSimple(
            StringUtil.getTitleForStep(context, lesson, step.position),
            StringUtil.getUriForStep(endpointResolver.getBaseUrl(), lesson, unit, step)
        )
}