package org.stepic.droid.view.custom;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

//todo: this class has a bad implementation, best practice use @Singleton
public class FontFactory {
    private static FontFactory instance;
    private HashMap<String, Typeface> fontMap;

    private FontFactory() {
        // singleton
        fontMap = new HashMap<>();
    }

    public static FontFactory getInstance() {
        if (instance == null) {
            instance = new FontFactory();
        }
        return instance;
    }


    public Typeface getFont(Context context, String font) {
        Typeface typeface = fontMap.get(font);
        if (typeface == null) {
            try{
                typeface = Typeface.createFromAsset(context.getResources()
                        .getAssets(), "fonts/" + font);
                fontMap.put(font, typeface);
            }catch (Exception ignored){
            }
        }
        return typeface;
    }
}