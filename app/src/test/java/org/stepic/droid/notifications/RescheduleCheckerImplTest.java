package org.stepic.droid.notifications;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RescheduleCheckerImplTest {

    //test this class
    private RescheduleChecker rescheduleChecker;
    private final int defaultStartHour = 23;
    private final int defaultEndHour = 8;


    @Before
    public void beforeEachTest() {
        rescheduleChecker = new RescheduleCheckerImpl(defaultStartHour, defaultEndHour);
    }


    @Test
    public void isRescheduleNeed_inInterval_true() {
        long millis = DateTime.now().withHourOfDay(0).getMillis();
        boolean result = rescheduleChecker.isRescheduleNeed(millis);
        assertTrue(result);
    }

    @Test
    public void isRescheduleNeed_outsideInterval_false() {
        long millis = DateTime.now().withHourOfDay(10).getMillis();
        boolean result = rescheduleChecker.isRescheduleNeed(millis);
        assertFalse(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isRescheduleNeedInterval_negativeInterval_throwIllegalArgumentException() {
        rescheduleChecker = new RescheduleCheckerImpl(-2, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isRescheduleNeedInterval_bigNumber_throwIllegalArgumentException() {
        rescheduleChecker = new RescheduleCheckerImpl(25, 144);
    }

    @Test
    public void isRescheduleNeed_upperBound_false() {
        long millis = DateTime.now().withHourOfDay(defaultEndHour).withMinuteOfHour(20).getMillis();
        boolean result = rescheduleChecker.isRescheduleNeed(millis);
        assertFalse(result);
    }

    @Test
    public void isRescheduleNeed_lowerBound_true() {
        long millis = DateTime.now().withHourOfDay(defaultStartHour).withMinuteOfHour(20).getMillis();
        boolean result = rescheduleChecker.isRescheduleNeed(millis);
        assertTrue(result);
    }

    @Test
    public void isRescheduleNeed_equalIntervalAndValueInThatHout_false() {
        rescheduleChecker = new RescheduleCheckerImpl(1, 1);
        long millis = DateTime.now().withHourOfDay(1).withMinuteOfHour(15).getMillis();
        boolean result = rescheduleChecker.isRescheduleNeed(millis);
        assertFalse(result);
    }


}
