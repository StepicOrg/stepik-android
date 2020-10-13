package org.stepik.android.domain.review_instruction.interactor

import org.stepik.android.domain.review_instruction.repository.ReviewInstructionRepository
import javax.inject.Inject

class ReviewInstructionInteractor
@Inject
constructor(
    private val reviewInstructionRepository: ReviewInstructionRepository
)