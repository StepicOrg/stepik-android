package org.stepik.android.domain.filter_search

data class Price(val start: Int, val end: Int) {
    companion object {
        const val NOT_SET = -1
    }
}
