package org.stepic.droid.analytic;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticImpl implements Analytic{
    private FirebaseAnalytics firebaseAnalytics;

    public AnalyticImpl(Context context){
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
}
