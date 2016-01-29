package org.stepic.droid.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JsonHelperTest {

    @Test
    public void toJson_NullObject_ReturnsEmptyString() {
        assertTrue(JsonHelper.toJson(null).isEmpty());
    }
}
