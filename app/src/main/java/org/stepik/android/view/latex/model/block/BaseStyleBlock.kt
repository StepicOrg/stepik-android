package org.stepik.android.view.latex.model.block

import androidx.annotation.ColorInt
import org.stepik.android.domain.latex.model.block.ContentBlock

class BaseStyleBlock(
    isNightMode: Boolean,
    fontPath: String,
    @ColorInt
    private val textColor: Int
) : ContentBlock {
    override val header: String = """
        <link rel="stylesheet" type="text/css" href="file:///android_asset/css/wysiwyg.css"/>
        <link rel="stylesheet" type="text/css" href="file:///android_asset/css/hljs${if (isNightMode) "-night" else ""}.css"/>
        
        <style>
            @font-face {
                font-family: 'Roboto';
                src: url("$fontPath")
            }
        
            html{-webkit-text-size-adjust: 100%%;}
            body{
                font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;
                color: ${formatRGBA(textColor)};
            }
            h1{font-size: 22px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}
            h2{font-size: 19px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}
            h3{font-size: 16px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}
            img { max-width: 100%%; }
        </style>
    """.trimIndent()

    private fun formatRGBA(@ColorInt color: Int): String {
        val b = color and 0xFF
        val g = (color shr 8) and 0xFF
        val r = (color shr 16) and 0xFF
        val a = (color shr 24) and 0xFF

        return "rgba($r, $g, $b, ${a.toDouble() / 0xFF})"
    }
}