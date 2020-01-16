package org.stepik.android.view.base.ui.widget.block

class LatexScriptBlock : ContentBlock {
    override val header: String = """
        <link rel="stylesheet" href="file:///android_asset/katex/katex.min.css" />
        <script src="file:///android_asset/katex/katex.min.js" ></script>
        <script src="file:///android_asset/katex/auto-render.min.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", function() {
                renderMathInElement(document.body, {
                    delimiters: [
                          {left: "$$", right: "$$", display: true},
                          {left: "\\[", right: "\\]", display: true},
                          {left: "$", right: "$", display: false},
                          {left: "\\(", right: "\\)", display: false}
                    ]
                });
            });
        </script>;
    """.trimIndent()
        
    override fun isEnabled(content: String): Boolean =
        "$" in content ||
        "\\[" in content ||
        "math-tex" in content ||
        "\\(" in content
}