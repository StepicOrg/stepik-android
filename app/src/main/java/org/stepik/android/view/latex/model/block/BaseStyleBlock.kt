package org.stepik.android.view.latex.model.block

import org.stepik.android.domain.latex.model.block.ContentBlock

class BaseStyleBlock(
    fontPath: String
) : ContentBlock {
    override val header: String = """
        <link rel="stylesheet" type="text/css" href="wysiwyg.css"/>
        
        <style>
            @font-face {
                font-family: 'Roboto';
                src: url("$fontPath")
            }
        
            html{-webkit-text-size-adjust: 100%%;}
            body{font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;}
            h1{font-size: 22px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}
            h2{font-size: 19px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}
            h3{font-size: 16px; font-family:'Roboto', Helvetica, sans-serif; line-height:1.6em;text-align: center;}
            img { max-width: 100%%; }
        </style>
    """.trimIndent()

    override fun isEnabled(content: String): Boolean = true
}