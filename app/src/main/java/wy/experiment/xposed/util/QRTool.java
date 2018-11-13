package wy.experiment.xposed.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by wy on 2018/11/13.
 */

public class QRTool {
    private static final String TAG = "QRTool";
    private Context mContext;
    private static float scale;
    private static QRTool _instance;

    public static QRTool instance() {
        if (_instance == null) {
            synchronized (QRTool.class) {
                if (_instance == null) {
                    _instance = new QRTool();
                }
            }
        }
        return _instance;
    }

    public void init(Context context){
        mContext = context;
        try {
            if (scale == 0.0f) {
                scale = context.getResources().getDisplayMetrics().density;
            }
        } catch (Throwable th) {
            Log.e(TAG, th.getMessage());
        }
    }

    public int getQRCodeWidth(){
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels / 2;
        return (int) ((displayWidth - dip2px(15.0f) << 1) * 0.54d);
    }

    private int dip2px(float f) {
        return (int) ((scale * f) + 0.5f);
    }
}
