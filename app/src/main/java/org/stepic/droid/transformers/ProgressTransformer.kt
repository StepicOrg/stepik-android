package org.stepic.droid.transformers

import org.stepic.droid.model.Progress
import org.stepic.droid.util.AppConstants

import viewmodel.ProgressViewModel
import java.util.*

fun Progress?.transformToViewModel(): ProgressViewModel? {
    if (this == null || this.id == null) return null

    val doubleScore: Double
    val scoreString: String
    doubleScore = java.lang.Double.parseDouble(this.score)
    if (doubleScore == Math.floor(doubleScore) && !java.lang.Double.isInfinite(doubleScore)) {
        scoreString = doubleScore.toInt().toString() + ""
    } else {
        scoreString = String.format(Locale.getDefault(), "%.2f", doubleScore)
    }


    val sb = StringBuilder();
    sb.append(scoreString);
    sb.append(AppConstants.DELIMITER_TEXT_SCORE);
    sb.append(cost);
    val textForScoreAndCost = sb.toString()

    return ProgressViewModel(this.id!!, textForScoreAndCost, doubleScore.toInt(), this.cost)
}

