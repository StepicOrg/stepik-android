package org.stepic.droid.code.highlight.themes


object Presets {
    val GitHubTheme = CodeTheme(
            CodeSyntax(
                    plain = 0xFF333333.toInt(),
                    string = 0xFFd14d14.toInt(),
                    comment = 0xFF998998.toInt(),
                    type = 0xFF458458.toInt(),
                    literal = 0xFF458458.toInt(),
                    attributeName = 0xFF000080.toInt(),
                    attributeValue = 0xFFd14d14.toInt()
            ),
            background = 0xFFFFFFFF.toInt(),
            lineNumberBackground = 0xFFEEEEEE.toInt(),
            lineNumberText = 0xFF333333.toInt(),
            selectedLineBackground = 0x44CCCCCC,
            lineNumberStroke = 0xFFCCCCCC.toInt()
    )

    val DefaultTheme = GitHubTheme.copy(
            syntax = CodeSyntax(
                    plain = 0xFF333333.toInt(),
                    string = 0xFFc18401.toInt(),
                    keyword = 0xFF0184bc.toInt(),
                    comment = 0xFF999999.toInt(),
                    literal = 0xFF50a14f.toInt(),
                    punctuation = 0xFF4078f2.toInt()
            )
    )

    val TomorrowNightEighteisTheme = CodeTheme(
            syntax = CodeSyntax(
                    plain = 0xFFcccccc.toInt(),
                    string = 0xFF99cc99.toInt(),
                    keyword = 0xFFcc99cc.toInt(),
                    comment = 0xFF999999.toInt(),
                    type = 0xFF6699cc.toInt(),
                    literal = 0xFFf99157.toInt(),
                    tag = 0xFFf2777a.toInt(),
                    attributeName = 0xFFf99157.toInt(),
                    attributeValue = 0xFF66cccc.toInt(),
                    declaration = 0xFFf99157.toInt()
            ),
            background = 0xFF2d2d2d.toInt(),
            lineNumberBackground = 0x44999999,
            lineNumberText = 0xFFcccccc.toInt(),
            selectedLineBackground = 0x44CCCCCC,
            lineNumberStroke = 0x55999999
    )

    val TranquilHeartTheme = CodeTheme(
            syntax = CodeSyntax(
                    plain = 0xFFe6e9ed.toInt(),
                    string = 0xFFffce54.toInt(),
                    keyword = 0xFF4fc1e9.toInt(),
                    comment = 0xFF656d78.toInt(),
                    type = 0xFF4fc1e9.toInt(),
                    literal = 0xFFac92ec.toInt(),
                    tag = 0xFFed5565.toInt(),
                    attributeName = 0xFFa0d468.toInt(),
                    attributeValue = 0xFFffce54.toInt(),
                    declaration = 0xFFac92ec.toInt()
            ),
            background = 0xFF2f3640.toInt(),
            lineNumberBackground = 0xFF656d78.toInt(),
            selectedLineBackground = 0x44e6e9ed,
            lineNumberStroke = 0xFF656d78.toInt(),
            lineNumberText = 0xFFe6e9ed.toInt()
    )
}