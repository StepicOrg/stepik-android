package com.elpatika.stepic.view.custom;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

public class FontFactory {
    private static FontFactory instance;
    private HashMap<String, Typeface> fontMap = new HashMap<String, Typeface>();

    private FontFactory() {
        // singleton
    }

    public static FontFactory getInstance() {
        if (instance == null) {
            instance = new FontFactory();
        }
        return instance;
    }

    /**
     * Returns TypeFace for the given font name. Font file must exist in
     * assets/fonts folder.
     *
     * @param context
     * @return
     */
    public Typeface getFont(Context context, String font) {
        Typeface typeface = fontMap.get(font);
        if (typeface == null) {
            try{
                typeface = Typeface.createFromAsset(context.getResources()
                        .getAssets(), "fonts/" + font);
                fontMap.put(font, typeface);
            }catch (Exception ex){
            }
        }
        return typeface;
    }
}