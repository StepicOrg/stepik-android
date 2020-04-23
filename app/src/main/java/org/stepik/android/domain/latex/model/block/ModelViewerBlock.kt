package org.stepik.android.domain.latex.model.block

import org.stepik.android.view.latex.js_interface.ModelViewerInterface

class ModelViewerBlock : ContentBlock {
    override val header: String = """
        <script type="text/javascript">
            function handleARModel(url) {
                ${ModelViewerInterface.MODEL_VIEWER_INTERFACE}.handleARModel(url);
            }
        </script>
    """.trimIndent()

    // TODO Could check for `<model-viewer in content`, but rules replace it with `<div>`?
    override fun isEnabled(content: String): Boolean =
        "handleARModel" in content
}