package org.stepic.droid.util.resolvers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;

import org.stepic.droid.R;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class StepTypeResolver implements IStepResolver {

    private Map<String, Drawable> mapFromTypeToDrawable;
    private Context mContext;

    public StepTypeResolver(Context context) {

        mContext = context;
        mapFromTypeToDrawable = new HashMap<>();


        mapFromTypeToDrawable.put("text", getDrawable(context, R.drawable.ic_theory));
        mapFromTypeToDrawable.put("video", getDrawable(context, R.drawable.ic_video));
        mapFromTypeToDrawable.put("matching", getDrawable(context, R.drawable.ic_matching));
        mapFromTypeToDrawable.put("sorting", getDrawable(context, R.drawable.ic_sorting));
        mapFromTypeToDrawable.put("match", getDrawable(context, R.drawable.ic_math));
        mapFromTypeToDrawable.put("free-answer", getDrawable(context, R.drawable.ic_free_answer));
        mapFromTypeToDrawable.put("table", getDrawable(context, R.drawable.ic_table));
        mapFromTypeToDrawable.put("string", getDrawable(context, R.drawable.ic_string));
        mapFromTypeToDrawable.put("choice", getDrawable(context, R.drawable.ic_choice));
        mapFromTypeToDrawable.put("number", getDrawable(context, R.drawable.ic_number));
        mapFromTypeToDrawable.put("dataset", getDrawable(context, R.drawable.ic_dataset));
        mapFromTypeToDrawable.put("animation", getDrawable(context, R.drawable.ic_animation));
        mapFromTypeToDrawable.put("chemical", getDrawable(context, R.drawable.ic_chemical));
        mapFromTypeToDrawable.put("fill-blanks", getDrawable(context, R.drawable.ic_fill_blanks));
        mapFromTypeToDrawable.put("puzzle", getDrawable(context, R.drawable.ic_puzzle));
        mapFromTypeToDrawable.put("pycharm", getDrawable(context, R.drawable.ic_pycharm));
        mapFromTypeToDrawable.put("code", getDrawable(context, R.drawable.ic_code));

    }

    public Drawable getDrawableForType(String type, boolean viewed) {
        //todo:two maps for viewed and not, if viewved 1st map, not viewed second?
        Drawable drawable = mapFromTypeToDrawable.get(type);
        if (drawable != null)
            return drawable;
        else
            return getDrawable(mContext, R.drawable.ic_theory);
    }

    private Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(drawableRes);
        } else {
            return context.getResources().getDrawable(drawableRes);
        }
    }

}
