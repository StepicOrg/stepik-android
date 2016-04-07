package org.stepic.droid.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HtmlHelperTest {
    @Test
    public void notificationTest() {
        String htmlRaw = " В курсе <a href=\"/course/%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432/\">Школьная физика. Тепловые и электромагнитные явления.</a> открылся новый модуль <a href=\"/course/%D0%A8%D0%BA%D0%BE%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F-%D1%84%D0%B8%D0%B7%D0%B8%D0%BA%D0%B0-%D0%A2%D0%B5%D0%BF%D0%BB%D0%BE%D0%B2%D1%8B%D0%B5-%D0%B8-%D1%8D%D0%BB%D0%B5%D0%BA%D1%82%D1%80%D0%BE%D0%BC%D0%B0%D0%B3%D0%BD%D0%B8%D1%82%D0%BD%D1%8B%D0%B5-%D1%8F%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D1%8F-432/\">Постоянный ток</a> ";
        Long id = HtmlHelper.parseCourseIdFromNotification(htmlRaw);
        assertNotNull(id);
        assertEquals(id.longValue(), 432L);
    }
}
