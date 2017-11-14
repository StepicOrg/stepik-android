package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Section;

import java.util.List;

public class SectionsMetaResponse extends MetaResponseBase {

    private List<Section> sections;

    public SectionsMetaResponse(List<Section> sections, Meta meta) {
        super(meta);
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }
}
