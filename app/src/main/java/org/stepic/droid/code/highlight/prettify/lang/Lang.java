// Copyright (C) 2011 Chan Wai Shing
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.stepic.droid.code.highlight.prettify.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Lang class for Java Prettify.
 * Note that the method {@link #getFileExtensions()} should be overridden.
 * 
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public abstract class Lang {

    /**
     * Similar to those in JavaScript prettify.js.
     */
    protected List<List<Object>> shortcutStylePatterns;
    /**
     * Similar to those in JavaScript org.stepic.droid.code.highlight.prettify.js.
     */
    protected List<List<Object>> fallthroughStylePatterns;
    /**
     * See {@link org.stepic.droid.code.highlight.prettify.lang.LangCss} for example.
     */
    protected List<Lang> extendedLangs;

    /**
     * Constructor.
     */
    public Lang() {
        shortcutStylePatterns = new ArrayList<>();
        fallthroughStylePatterns = new ArrayList<>();
        extendedLangs = new ArrayList<>();
    }

    /**
     * This method should be overridden by the child class.
     * This provide the file extensions list to help the parser to determine which
     * {@link Lang} to use. See JavaScript prettify.js.
     *
     * @return the list of file extensions
     */
    public List<String> getFileExtensions() {
        return new ArrayList<>();
    }

    public List<List<Object>> getShortcutStylePatterns() {
        List<List<Object>> returnList = new ArrayList<List<Object>>();
        for (List<Object> shortcutStylePattern : shortcutStylePatterns) {
            returnList.add(new ArrayList<>(shortcutStylePattern));
        }
        return returnList;
    }

    public void setShortcutStylePatterns(List<List<Object>> shortcutStylePatterns) {
        if (shortcutStylePatterns == null) {
            this.shortcutStylePatterns = new ArrayList<>();
            return;
        }
        List<List<Object>> cloneList = new ArrayList<List<Object>>();
        for (List<Object> shortcutStylePattern : shortcutStylePatterns) {
            cloneList.add(new ArrayList<>(shortcutStylePattern));
        }
        this.shortcutStylePatterns = cloneList;
    }

    public List<List<Object>> getFallthroughStylePatterns() {
        List<List<Object>> returnList = new ArrayList<List<Object>>();
        for (List<Object> fallthroughStylePattern : fallthroughStylePatterns) {
            returnList.add(new ArrayList<>(fallthroughStylePattern));
        }
        return returnList;
    }

    public void setFallthroughStylePatterns(List<List<Object>> fallthroughStylePatterns) {
        if (fallthroughStylePatterns == null) {
            this.fallthroughStylePatterns = new ArrayList<>();
            return;
        }
        List<List<Object>> cloneList = new ArrayList<>();
        for (List<Object> fallthroughStylePattern : fallthroughStylePatterns) {
            cloneList.add(new ArrayList<>(fallthroughStylePattern));
        }
        this.fallthroughStylePatterns = cloneList;
    }

    /**
     * Get the extended languages list.
     * @return the list
     */
    public List<Lang> getExtendedLangs() {
        return new ArrayList<>(extendedLangs);
    }

    /**
     * Set extended languages. Because we cannot register multiple languages
     * within one {@link org.stepic.droid.code.highlight.prettify.lang.Lang}, so it is used as an solution. See
     * {@link org.stepic.droid.code.highlight.prettify.lang.LangCss} for example.
     *
     * @param extendedLangs the list of {@link org.stepic.droid.code.highlight.prettify.lang.Lang}s
     */
    public void setExtendedLangs(List<Lang> extendedLangs) {
        this.extendedLangs = new ArrayList<>(extendedLangs);
    }
}
