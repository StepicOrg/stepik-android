package org.stepic.droid.code.highlight.prettify.lang;

import org.stepic.droid.code.highlight.prettify.parser.Prettify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class LangKotlin extends Lang {

    public LangKotlin() {
        List<List<Object>> _shortcutStylePatterns = new ArrayList<>();
        List<List<Object>> _fallthroughStylePatterns = new ArrayList<>();

        _shortcutStylePatterns.add(Arrays.<Object>asList(Prettify.PR_PLAIN, Pattern.compile("^[\\t\\n\\r \\xA0]"), null, "\t\n\r " + Character.toString((char) 0xA0)));
        _shortcutStylePatterns.add(Arrays.<Object>asList(Prettify.PR_PUNCTUATION, Pattern.compile("^[.!%&()*+,\\-;<=>?\\[\\\\\\]^{|}:]+"), null, ".!%&()*+,-;<=>?[\\]^{|}:"));


        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_KEYWORD, Pattern.compile("^\\b(package|public|protected|private|open|abstract|constructor|final|override|import|for|while|by|and|or|as|typealias|get|set|((data|enum|annotation|sealed) )?class|this|super|const|val|var|fun|is|in|throw|return|break|continue|(companion )?object|if|try|else|do|when|init|interface|typeof)\\b")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_LITERAL, Pattern.compile("^(?:true|false|null)\\b")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_LITERAL, Pattern.compile("^(0[xX][0-9a-fA-F_]+L?|0[bB][0-1]+L?|[0-9\\.][0-9_\\.]+([eE]-?[0-9]+)?[fFL]?)")));
        // TODO improve numbers parsing

        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_TYPE, Pattern.compile("^(\\b[A-Z]+[a-z][a-zA-Z0-9_$@]*|`.*`)")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_COMMENT, Pattern.compile("^\\/\\/.*")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_COMMENT, Pattern.compile("^\\/\\*[\\s\\S]*?(?:\\*\\/|$)")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_STRING, Pattern.compile("'.'")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_STRING, Pattern.compile("^\"{3}[\\s\\S]*?[^\\\\]\"{3}")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_STRING, Pattern.compile("^\"([^\"\\\\]|\\\\[\\s\\S])*\"")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_LITERAL, Pattern.compile("^@([a-zA-Z0-9_$@]*|`.*`)")));
        _fallthroughStylePatterns.add(Arrays.<Object>asList(Prettify.PR_LITERAL, Pattern.compile("^[a-zA-Z0-9_]+@")));


        setShortcutStylePatterns(_shortcutStylePatterns);
        setFallthroughStylePatterns(_fallthroughStylePatterns);
    }

    public static List<String> getFileExtensions() {
        return Collections.singletonList("kt");
    }
}
