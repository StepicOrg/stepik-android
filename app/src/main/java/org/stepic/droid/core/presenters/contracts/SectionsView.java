package org.stepic.droid.core.presenters.contracts;

import org.stepic.droid.model.Section;

import java.util.List;

public interface SectionsView {

    void onEmptySections();

    void onConnectionProblem();

    void onNeedShowSections(List<Section> sectionList);

    void onLoading();

}
