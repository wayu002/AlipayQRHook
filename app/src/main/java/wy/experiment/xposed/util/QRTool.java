package wy.experiment.xposed.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public String saveImage(Bitmap bmp, String fileName, String dirName, int type) {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xpcracker/" + dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            if(type == 0) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 40, fos);
            }else {
                bmp.compress(Bitmap.CompressFormat.PNG, 40, fos);
            }
            fos.flush();
            fos.close();
            Log.d(TAG, "success save image: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public void sendQRComplete(boolean success, String path, Messenger service){
        Message msg = Message.obtain(null, XPConstant.MSG_QR_COMPLETE);
        Bundle data = new Bundle();
        data.putBoolean(XPConstant.QR_SUCCESS, success);
        if(success){
            data.putString(XPConstant.QR_PATH, path);
        }
        msg.setData(data);
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int dip2px(float f) {
        return (int) ((scale * f) + 0.5f);
    }
}
