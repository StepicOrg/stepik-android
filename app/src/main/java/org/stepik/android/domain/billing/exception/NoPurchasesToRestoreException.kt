package org.stepik.android.domain.billing.exception

import java.lang.IllegalStateException

class NoPurchasesToRestoreException : IllegalStateException("There is no purchases that can be restored")