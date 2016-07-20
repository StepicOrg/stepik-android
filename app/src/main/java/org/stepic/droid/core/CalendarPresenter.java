package org.stepic.droid.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Section;

import java.util.List;

public interface CalendarPresenter {

    /**
     * true, if any section in list have deadline (soft or hard) and the deadline is not happened.
     * false, otherwise.
     *
     * @param sectionList list of sections of course
     */
    boolean shouldBeShown(@Nullable List<Section> sectionList);

    /**
     * Same as {@link #shouldBeShown(List)} , but check preferences of the user (want he/she get calendar widget)
     */
    boolean shouldBeShownAsWidget(@Nullable List<Section> sectionList);

    /**
     * add soft and hard deadline to calendar, if permission not granted put it to {@code exportableView}
     *
     * @param sectionList list of sections of course
     */
    void addDeadlinesToCalendar(@NotNull List<Section> sectionList);

    void onStart(CalendarExportableView view);

    void onStop();
}