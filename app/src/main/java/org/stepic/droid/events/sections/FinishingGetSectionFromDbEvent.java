package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;

import java.util.List;

public class FinishingGetSectionFromDbEvent extends SectionBaseEvent {
    List<Section> mSectionList;
    public FinishingGetSectionFromDbEvent(Course course, List<Section> sections) {
        super(course);
        mSectionList = sections;
    }

    public List<Section> getSectionList() {
        return mSectionList;
    }
}
