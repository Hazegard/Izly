package android.support.v4.graphics.drawable;

import android.graphics.drawable.Drawable;
import android.util.Log;
import java.lang.reflect.Method;

class DrawableCompatJellybeanMr1 {
    private static final String TAG = "DrawableCompatJellybeanMr1";
    private static Method sGetLayoutDirectionMethod;
    private static boolean sGetLayoutDirectionMethodFetched;
    private static Method sSetLayoutDirectionMethod;
    private static boolean sSetLayoutDirectionMethodFetched;

    DrawableCompatJellybeanMr1() {
    }

    public static int getLayoutDirection(Drawable drawable) {
        if (!sGetLayoutDirectionMethodFetched) {
            try {
                Method declaredMethod = Drawable.class.getDeclaredMethod("getLayoutDirection", new Class[0]);
                sGetLayoutDirectionMethod = declaredMethod;
                declaredMethod.setAccessible(true);
            } catch (Throwable e) {
                Log.i(TAG, "Failed to retrieve getLayoutDirection() method", e);
            }
            sGetLayoutDirectionMethodFetched = true;
        }
        if (sGetLayoutDirectionMethod != null) {
            try {
                return ((Integer) sGetLayoutDirectionMethod.invoke(drawable, new Object[0])).intValue();
            } catch (Throwable e2) {
                Log.i(TAG, "Failed to invoke getLayoutDirection() via reflection", e2);
                sGetLayoutDirectionMethod = null;
            }
        }
        return -1;
    }

    public static void setLayoutDirection(Drawable drawable, int i) {
        if (!sSetLayoutDirectionMethodFetched) {
            try {
                Method declaredMethod = Drawable.class.getDeclaredMethod("setLayoutDirection", new Class[]{Integer.TYPE});
                sSetLayoutDirectionMethod = declaredMethod;
                declaredMethod.setAccessible(true);
            } catch (Throwable e) {
                Log.i(TAG, "Failed to retrieve setLayoutDirection(int) method", e);
            }
            sSetLayoutDirectionMethodFetched = true;
        }
        if (sSetLayoutDirectionMethod != null) {
            try {
                sSetLayoutDirectionMethod.invoke(drawable, new Object[]{Integer.valueOf(i)});
            } catch (Throwable e2) {
                Log.i(TAG, "Failed to invoke setLayoutDirection(int) via reflection", e2);
                sSetLayoutDirectionMethod = null;
            }
        }
    }
}
