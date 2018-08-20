package org.groundwork.downtime;

import com.groundwork.downtime.DowntimeMaintenanceWindow;
import com.groundwork.downtime.http.TransitionWindowCalculator;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DowntimeWindowCalculatorTest {

    @Test
    public void testSingleWindowInTheMiddleWithTwoPaddedGaps() throws Exception {
        Date startRange = new GregorianCalendar(2018, Calendar.JUNE, 11, 0, 0).getTime();
        Date endRange = new GregorianCalendar(2018, Calendar.JUNE, 15, 23, 59).getTime();
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        DowntimeMaintenanceWindow window1 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 13, 0, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 14, 6, 0).getTime());
        window1.setPercentage(calcPercentage(startRange, endRange, window1));
        downTimesPerService.add(window1);

        List<DowntimeMaintenanceWindow> downtimesWithGaps = TransitionWindowCalculator.addGapsToWindowList(downTimesPerService, startRange, endRange);
        assertEquals(downtimesWithGaps.size(), 3);
        assertEquals(downtimesWithGaps.get(0).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(1).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Active);
        assertEquals(downtimesWithGaps.get(2).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(sum(downtimesWithGaps), 100);
    }

    @Test
    public void testSingleWindowBeforeSingleGap() throws Exception {
        Date startRange = new GregorianCalendar(2018, Calendar.JUNE, 11, 0, 0).getTime();
        Date endRange = new GregorianCalendar(2018, Calendar.JUNE, 15, 23, 59).getTime();
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        DowntimeMaintenanceWindow window1 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Expired, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 10, 0, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 14, 6, 0).getTime());
        window1.setPercentage(calcPercentage(startRange, endRange, window1));
        downTimesPerService.add(window1);
        List<DowntimeMaintenanceWindow> downtimesWithGaps = TransitionWindowCalculator.addGapsToWindowList(downTimesPerService, startRange, endRange);
        assertEquals(downtimesWithGaps.size(), 2);
        assertEquals(downtimesWithGaps.get(0).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Expired);
        assertEquals(downtimesWithGaps.get(1).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(sum(downtimesWithGaps), 100);
    }

    @Test
    public void testSingleGapBeforeSingleWindow() throws Exception {
        Date startRange = new GregorianCalendar(2018, Calendar.JUNE, 11, 0, 0).getTime();
        Date endRange = new GregorianCalendar(2018, Calendar.JUNE, 15, 23, 59).getTime();
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        DowntimeMaintenanceWindow window1 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Pending, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 13, 0, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 16, 6, 0).getTime());
        window1.setPercentage(calcPercentage(startRange, endRange, window1));
        downTimesPerService.add(window1);

        List<DowntimeMaintenanceWindow> downtimesWithGaps = TransitionWindowCalculator.addGapsToWindowList(downTimesPerService, startRange, endRange);
        assertEquals(downtimesWithGaps.size(), 2);
        assertEquals(downtimesWithGaps.get(0).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(1).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Pending);
        assertEquals(sum(downtimesWithGaps), 100);
    }

    @Test
    public void testDoubleWindowInTheMiddleWithThreePaddedGaps() throws Exception {
        Date startRange = new GregorianCalendar(2018, Calendar.JUNE, 10, 0, 0).getTime();
        Date endRange = new GregorianCalendar(2018, Calendar.JUNE, 16, 23, 59).getTime();
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        DowntimeMaintenanceWindow window1 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 12, 0, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 13, 6, 0).getTime());
        window1.setPercentage(calcPercentage(startRange, endRange, window1));
        downTimesPerService.add(window1);

        DowntimeMaintenanceWindow window2 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 13, 8, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 14, 23, 0).getTime());
        window2.setPercentage(calcPercentage(startRange, endRange, window2));
        downTimesPerService.add(window2);

        List<DowntimeMaintenanceWindow> downtimesWithGaps = TransitionWindowCalculator.addGapsToWindowList(downTimesPerService, startRange, endRange);
        assertEquals(downtimesWithGaps.size(), 5);
        assertEquals(downtimesWithGaps.get(0).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(1).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Active);
        assertEquals(downtimesWithGaps.get(2).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(3).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Active);
        assertEquals(downtimesWithGaps.get(4).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(sum(downtimesWithGaps), 100);
    }

    @Test
    public void testDoubleWindowInTheMiddleTwoPaddedGapsAndOneEndingWindow() throws Exception {
        Date startRange = new GregorianCalendar(2018, Calendar.JUNE, 12, 0, 0).getTime();
        Date endRange = new GregorianCalendar(2018, Calendar.JUNE, 17, 23, 59).getTime();
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        DowntimeMaintenanceWindow window1 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 12, 14, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 13, 6, 0).getTime());
        window1.setPercentage(calcPercentage(startRange, endRange, window1));
        downTimesPerService.add(window1);

        DowntimeMaintenanceWindow window2 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Active, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 13, 8, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 14, 23, 0).getTime());
        window2.setPercentage(calcPercentage(startRange, endRange, window2));
        downTimesPerService.add(window2);

        DowntimeMaintenanceWindow window3 = new DowntimeMaintenanceWindow(DowntimeMaintenanceWindow.MaintenanceStatus.Pending, 0.0f, "test one",
                new GregorianCalendar(2018, Calendar.JUNE, 15, 8, 0).getTime(),
                new GregorianCalendar(2018, Calendar.JUNE, 18, 23, 0).getTime());
        window3.setPercentage(calcPercentage(startRange, endRange, window3));
        downTimesPerService.add(window3);

        List<DowntimeMaintenanceWindow> downtimesWithGaps = TransitionWindowCalculator.addGapsToWindowList(downTimesPerService, startRange, endRange);
        assertEquals(downtimesWithGaps.size(), 6);
        assertEquals(downtimesWithGaps.get(0).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(1).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Active);
        assertEquals(downtimesWithGaps.get(2).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(3).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Active);
        assertEquals(downtimesWithGaps.get(4).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(downtimesWithGaps.get(5).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.Pending);
        assertEquals(sum(downtimesWithGaps), 100);
    }

    @Test
    public void testEmpty() throws Exception {
        Date startRange = new GregorianCalendar(2018, Calendar.JUNE, 11, 0, 0).getTime();
        Date endRange = new GregorianCalendar(2018, Calendar.JUNE, 15, 23, 59).getTime();
        List<DowntimeMaintenanceWindow> downTimesPerService = new LinkedList<>();
        List<DowntimeMaintenanceWindow> downtimesWithGaps = TransitionWindowCalculator.addGapsToWindowList(downTimesPerService, startRange, endRange);
        assertEquals(downtimesWithGaps.size(), 1);
        assertEquals(downtimesWithGaps.get(0).getStatus(), DowntimeMaintenanceWindow.MaintenanceStatus.None);
        assertEquals(sum(downtimesWithGaps), 100);
    }

    private float calcPercentage(Date startRange, Date endRange, DowntimeMaintenanceWindow window) {
        Date dtStart = window.getStartDate();
        Date dtEnd = window.getEndDate();
        if (dtStart.before(startRange)) {
            dtStart = startRange;
        }
        if (dtEnd.after(endRange)) {
            dtEnd = endRange;
        }
        float durationInState = dtEnd.getTime() - dtStart.getTime();
        float durationWindow = endRange.getTime() - startRange.getTime();
        float percentage = ((durationInState / durationWindow) * 100.0f);
        return percentage;
    }

    private int sum(List<DowntimeMaintenanceWindow> downtimesWithGaps) {
        float sum = 0;
        for (DowntimeMaintenanceWindow window : downtimesWithGaps) {
            sum += window.getPercentage();
        }
        return Math.round(sum);
    }
}
