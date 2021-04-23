package org.stepik.android.domain.latex.model.block

import org.stepik.android.domain.latex.model.Settings

class KotlinRunnableSamplesScriptBlock : ContentBlock {
    override val header: String = """
        <script src="https://unpkg.com/kotlin-playground@1"></script>
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                KotlinPlayground('kotlin-runnable', { 
                    callback: function(targetNode, mountNode) {
                        mountNode.classList.add('no-scroll');  // disable overflow for pg divs in order to disable overflow and show expand button
                    }
                });
            });
        </script>
    """.trimIndent()

    override val settings: Settings
        get() = Settings(allowUniversalAccessFromFileURLs = true)

    override fun isEnabled(content: String): Boolean =
        "kotlin-runnable" in content
}