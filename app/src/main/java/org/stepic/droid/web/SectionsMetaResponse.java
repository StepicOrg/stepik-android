package org.stepic.droid.web;

import org.stepik.android.model.structure.Section;
import org.stepik.android.model.Meta;

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
