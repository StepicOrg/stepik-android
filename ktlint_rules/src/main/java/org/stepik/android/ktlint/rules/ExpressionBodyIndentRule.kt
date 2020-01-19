package org.stepik.android.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.astReplace

class ExpressionBodyIndentRule : Rule("expression-body-indent") {
    companion object {
        private const val MAX_EXPRESSION_BODY_LEN = 10
        private const val INDENT_SIZE = 4
    }

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        when (val element = node.psi ?: return) {
            is KtNamedFunction -> {
                val eq = element.equalsToken ?: return
                val nextEq = eq.nextSibling

                if (nextEq is PsiWhiteSpace) {
                    val funPrev = element.prevSibling
                    val funIndent =
                        if (funPrev is PsiWhiteSpace) {
                            funPrev.text.split('\n').last().length
                        } else {
                            0
                        }

                    if (!nextEq.text.startsWith('\n')) {
                        if (nextEq.nextSibling.textLength > MAX_EXPRESSION_BODY_LEN) {
                            emit(node.startOffset, "Expression body should be written on the next line after method declaration", true)
                            if (autoCorrect) {
                                nextEq.astReplace(PsiWhiteSpaceImpl("\n" + " ".repeat(funIndent + INDENT_SIZE)))
                            }
                        }
                    } else {
                        if (funPrev is PsiWhiteSpace) {
                            val eqIndent = nextEq.text.split('\n').last().length

                            if (funIndent + INDENT_SIZE != eqIndent) {
                                emit(node.startOffset, "Unexpected indentation of expression body (expected ${funIndent + INDENT_SIZE}, actual $eqIndent) ", true)
                                if (autoCorrect) {
                                    nextEq.astReplace(PsiWhiteSpaceImpl("\n" + " ".repeat(funIndent + INDENT_SIZE)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}