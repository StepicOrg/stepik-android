package org.stepic.droid.persistence.content.processors

import org.junit.Assert
import org.junit.Test
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Block
import org.stepik.android.model.Step

class ImageStepContentProcessorTest {
    companion object {
        private const val REMOTE_IMAGE_LINK = "https://ucarecdn.com/3ac18be5-a69e-4ca9-b5ff-b1bbb80cc71f/"
        private const val LOCAL_IMAGE_LING = "local/image"
        private const val HTML_TEXT = """
            Вы можете добавить в этот шаг текст, а также изображения, математические формулы, примеры кода и многое другое.<img src="%s">
        """
    }

    private val contentProcessor = ImageStepContentProcessor()
    private val downloadConfiguration = DownloadConfiguration(videoQuality = "")

    private val step by lazy {
        val html = HTML_TEXT.format(REMOTE_IMAGE_LINK)
        Step(
            block = Block(text = html)
        )
    }

    @Test
    fun extractDownloadableContentTest() {
        val content = contentProcessor.extractDownloadableContent(step, downloadConfiguration)

        Assert.assertEquals(setOf(REMOTE_IMAGE_LINK), content)
    }

    @Test
    fun injectPersistentContentTest() {
        val stepWrapper = contentProcessor
            .injectPersistentContent(
                StepPersistentWrapper(step),
                mapOf(REMOTE_IMAGE_LINK to LOCAL_IMAGE_LING)
            )

        Assert.assertEquals(setOf("file://$LOCAL_IMAGE_LING"), contentProcessor.extractDownloadableContent(stepWrapper.step, downloadConfiguration))
    }


}