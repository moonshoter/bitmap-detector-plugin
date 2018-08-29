package com.moonshot.library.imagedetector;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

public class Utils {

    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        }
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return null;

    }
}
