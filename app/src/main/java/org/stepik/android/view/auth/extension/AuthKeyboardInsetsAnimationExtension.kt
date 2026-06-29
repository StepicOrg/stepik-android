package org.stepik.android.view.auth.extension

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import org.stepik.android.view.auth.ui.view.AuthScrollView

fun View.addAuthKeyboardInsetsAnimation(
    contentView: View,
    logoView: View,
    titleView: View,
    keyboardPinnedView: View? = null
) {
    AuthKeyboardInsetsAnimationDelegate(
        rootView = this,
        contentView = contentView,
        logoView = logoView,
        titleView = titleView,
        keyboardPinnedView = keyboardPinnedView
    ).bind()
}

private class AuthKeyboardInsetsAnimationDelegate(
    private val rootView: View,
    private val contentView: View,
    private val logoView: View,
    private val titleView: View,
    private val keyboardPinnedView: View?
) {
    private var isImeAnimationRunning = false
    private var lastImeBottomInset = 0
    private var isImeVisible = false

    private var isInitialStateCaptured = false

    private var logoHiddenTranslationY = 0f
    private var titleHiddenTranslationY = 0f
    private var contentHiddenTranslationY = 0f

    private var imeAnimationLowerBound = 0
    private var imeAnimationUpperBound = 0

    private var initialRootPaddingLeft = 0
    private var initialRootPaddingTop = 0
    private var initialRootPaddingRight = 0
    private var initialRootPaddingBottom = 0
    private var lastSystemBarsBottomInset = 0

    fun bind() {
        (rootView as? AuthScrollView)?.isFocusScrollEnabled = false

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBottomInset = getImeBottomInset(insets)
            isImeVisible = insets.isImeVisible()
            lastImeBottomInset = imeBottomInset
            lastSystemBarsBottomInset = systemBarsInsets.bottom

            if (isInitialStateCaptured) {
                applyInsetsPadding(systemBarsInsets)
            }

            if (!isImeAnimationRunning) {
                setKeyboardProgress(if (isImeVisible) 1f else 0f)
            }
            insets
        }

        ViewCompat.setWindowInsetsAnimationCallback(
            rootView,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onStart(
                    animation: WindowInsetsAnimationCompat,
                    bounds: WindowInsetsAnimationCompat.BoundsCompat
                ): WindowInsetsAnimationCompat.BoundsCompat {
                    if (animation.isImeInsetsAnimation()) {
                        isImeAnimationRunning = true
                        captureHiddenTranslations()
                        imeAnimationLowerBound = bounds.lowerBound.bottom
                        imeAnimationUpperBound = bounds.upperBound.bottom
                    }
                    return bounds
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeAnimation = runningAnimations.firstOrNull { it.isImeInsetsAnimation() }
                    if (imeAnimation != null) {
                        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        val imeBottomInset = getImeBottomInset(insets)
                        val animationProgress = imeAnimation.interpolatedFraction.coerceIn(0f, 1f)
                        lastImeBottomInset = imeBottomInset
                        lastSystemBarsBottomInset = systemBarsInsets.bottom

                        if (isInitialStateCaptured) {
                            applyInsetsPadding(systemBarsInsets)
                        }
                        setKeyboardProgress(resolveKeyboardProgress(imeBottomInset, animationProgress))
                    }

                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if (animation.isImeInsetsAnimation()) {
                        ViewCompat.getRootWindowInsets(rootView)?.let { insets ->
                            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                            isImeVisible = insets.isImeVisible()
                            lastImeBottomInset = getImeBottomInset(insets)
                            lastSystemBarsBottomInset = systemBarsInsets.bottom
                        }
                        isImeAnimationRunning = false
                        setKeyboardProgress(if (isImeVisible) 1f else 0f)
                        ViewCompat.requestApplyInsets(rootView)
                    }
                }
            }
        )

        rootView.doOnLayout {
            captureInitialState()
            ViewCompat.requestApplyInsets(rootView)
        }
    }

    private fun captureInitialState() {
        if (isInitialStateCaptured) {
            return
        }

        initialRootPaddingLeft = rootView.paddingLeft
        initialRootPaddingTop = rootView.paddingTop
        initialRootPaddingRight = rootView.paddingRight
        initialRootPaddingBottom = rootView.paddingBottom

        captureHiddenTranslations()

        isInitialStateCaptured = true
    }

    private fun captureHiddenTranslations() {
        logoHiddenTranslationY = -(logoView.top + logoView.height).toFloat()
        titleHiddenTranslationY = -(titleView.top + titleView.height).toFloat()
        contentHiddenTranslationY = -contentView.top.toFloat()
    }

    private fun setKeyboardProgress(progress: Float) {
        if (!isInitialStateCaptured) {
            return
        }

        val normalizedProgress = progress.coerceIn(0f, 1f)
        val headerVisibilityProgress = 1f - normalizedProgress

        logoView.alpha = headerVisibilityProgress
        titleView.alpha = headerVisibilityProgress

        logoView.visibility = View.VISIBLE
        titleView.visibility = View.VISIBLE

        logoView.translationY = logoHiddenTranslationY * normalizedProgress
        titleView.translationY = titleHiddenTranslationY * normalizedProgress
        contentView.translationY = contentHiddenTranslationY * normalizedProgress
        keyboardPinnedView?.translationY = -(lastImeBottomInset - lastSystemBarsBottomInset).coerceAtLeast(0).toFloat()
    }

    private fun resolveKeyboardProgress(
        imeBottomInset: Int,
        fallbackAnimationProgress: Float
    ): Float {
        val imeAnimationRange = imeAnimationUpperBound - imeAnimationLowerBound
        return if (imeAnimationRange > 0) {
            ((imeBottomInset - imeAnimationLowerBound).toFloat() / imeAnimationRange).coerceIn(0f, 1f)
        } else {
            fallbackAnimationProgress.coerceIn(0f, 1f)
        }
    }

    private fun applyInsetsPadding(systemBarsInsets: androidx.core.graphics.Insets) {
        rootView.setPadding(
            initialRootPaddingLeft + systemBarsInsets.left,
            initialRootPaddingTop + systemBarsInsets.top,
            initialRootPaddingRight + systemBarsInsets.right,
            initialRootPaddingBottom + systemBarsInsets.bottom
        )
    }

    private fun getImeBottomInset(insets: WindowInsetsCompat): Int =
        insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

    private fun WindowInsetsCompat.isImeVisible(): Boolean =
        isVisible(WindowInsetsCompat.Type.ime()) || getImeBottomInset(this) > 0

    private fun WindowInsetsAnimationCompat.isImeInsetsAnimation(): Boolean =
        typeMask and WindowInsetsCompat.Type.ime() != 0
}
