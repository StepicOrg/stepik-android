package org.stepic.droid.social;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public interface ISocialType extends Serializable {

    String getIdentifier();

    Drawable getIcon();

    boolean needUseAccessTokenInsteadOfCode();
}
