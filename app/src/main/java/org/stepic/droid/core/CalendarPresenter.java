package org.stepic.droid.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CalendarItem;
import org.stepic.droid.model.Section;

import java.util.List;

public interface CalendarPresenter {

    /**
     * true, if any section in list have deadline (soft or hard) and the deadline is not happened
     * and calendar was not added before. If course was restarted, than show (new_deadline - old_deadline > 1 month).
     * false, otherwise.
     *
     */
    void checkToShowCalendar(@Nullable List<Section> sectionList);

    /**
     * add soft and hard deadline to calendar, if permission not granted put it to {@code exportableView}
     *
     * @param sectionList list of sections of course
     */
    void addDeadlinesToCalendar(@NotNull List<Section> sectionList, @Nullable CalendarItem calendarItem);

    void onStart(CalendarExportableView view);

    void onStop();
}