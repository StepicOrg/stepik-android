package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.Section;

import java.util.List;

public class SectionsStepicResponse extends StepicResponseBase {

    List<Section> sections;

    public SectionsStepicResponse(List<Section> sections, Meta meta) {
        super(meta);
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }
}
