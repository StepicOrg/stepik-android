package org.stepik.android.ktlint.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReturnExpression

class ExpressionBodyRule : Rule("expression-body") {
    companion object {
        private const val INDENT_SIZE = 4
    }

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        when (val element = node.psi ?: return) {
            is KtNamedFunction -> {
                val body = element.bodyExpression as? KtBlockExpression ?: return
                val firstStatement = body.statements.firstOrNull()
                if (firstStatement is KtReturnExpression) {
                    emit(node.startOffset, "Use expression body instead of one line return", true)

                    if (autoCorrect) {
                        val funPrev = element.prevSibling
                        val indent =
                            if (funPrev is PsiWhiteSpace) {
                                funPrev.text.split('\n').last().length + INDENT_SIZE
                            } else {
                                INDENT_SIZE
                            }

                        val returnedExpression = firstStatement.returnedExpression ?: return
                        node.removeChild(node.lastChildNode)
                        node.addChild(LeafPsiElement(ElementType.EQ, "="), null)
                        node.addChild(PsiWhiteSpaceImpl(" "), null)
                        node.addChild(LeafPsiElement(ElementType.DANGLING_NEWLINE, "\n"), null)
                        node.addChild(PsiWhiteSpaceImpl(" ".repeat(indent)), null)
                        node.addChild(returnedExpression.node, null)
                    }
                }
            }
        }
    }
}