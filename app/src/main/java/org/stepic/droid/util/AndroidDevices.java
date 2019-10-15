/*****************************************************************************
 * AndroidDevices.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.stepic.droid.util;

import android.os.Environment;

import org.stepic.droid.base.App;

import java.util.HashSet;

public class AndroidDevices {
    public final static String TAG = "VLC/UiTools/AndroidDevices";
    public final static String EXTERNAL_PUBLIC_DIRECTORY = Environment.getExternalStorageDirectory().getPath();

    final static boolean hasNavBar;
    final static boolean hasTsp, isTv;

    static {
        HashSet<String> devicesWithoutNavBar = new HashSet<String>();
        devicesWithoutNavBar.add("HTC One V");
        devicesWithoutNavBar.add("HTC One S");
        devicesWithoutNavBar.add("HTC One X");
        devicesWithoutNavBar.add("HTC One XL");
        hasNavBar = !devicesWithoutNavBar.contains(android.os.Build.MODEL);
        hasTsp = App.Companion.getAppContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen");
        isTv = App.Companion.getAppContext().getPackageManager().hasSystemFeature("android.software.leanback");
    }

    public static boolean hasNavBar() {
        return hasNavBar;
    }
}
