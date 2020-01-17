package org.stepik.android.domain.latex.model.block

import android.os.Build

class HorizontalScrollBlock : ContentBlock {
    companion object {
        const val SCRIPT_NAME = "scrollListener"
        const val METHOD_NAME = "measureScroll"

        private val style = """
            <style>
                body > * {
                    max-width: 100%%;
                    overflow-x: scroll;
                    vertical-align: middle;
                }
                body > .no-scroll {
                    overflow: visible !important;
                }
            </style>
        """.trimIndent()

        private val script = """
            <script type="text/javascript">
                function $METHOD_NAME(x, y) {
                    var elem = document.elementFromPoint(x, y);
                    while(
                        elem.parentElement.tagName !== 'BODY' && 
                        elem.parentElement.tagName !== 'HTML' && 
                        elem.className !== 'CodeMirror-scroll' && 
                        elem.className !== 'code-output'
                    ) {
                        elem = elem.parentElement;
                    }
                    $SCRIPT_NAME.onScroll(elem.offsetWidth, elem.scrollWidth, elem.scrollLeft);
                }
            </script>
        """.trimIndent()
    }

    override val header: String = style + script

    override fun isEnabled(content: String): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}