package wy.experiment.xposed;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import wy.experiment.xposed.util.XPConstant;

/**
 * Created by wy on 2018/11/16.
 */

public class MessengerHandler extends Handler {
    private static final String TAG = "MessengerHandler";
    private WeakReference<ICracker> mCracker;

    public MessengerHandler(ICracker cracker){
        mCracker = new WeakReference<>(cracker);
    }

    @Override
    public void handleMessage(Message msg) {
        if(mCracker.get() == null || !mCracker.get().isReadyForUse()){
            Log.e(TAG, "ICracker is died");
            return;
        }
        switch (msg.what){
            case XPConstant.ALI_GENERATE_QR:
            case XPConstant.WE_GENERATE_QR:
                Log.d(TAG, "receive MSG_GENERATE_QR");
                if(mCracker.get() != null && mCracker.get().getQR() != null){
                    String desc = msg.getData().getString(XPConstant.QR_DES);
                    float money = msg.getData().getFloat(XPConstant.QR_MONEY, 0f);
                    Bitmap userIcon = msg.getData().getParcelable(XPConstant.QR_ICON);
                    mCracker.get().getQR().generateQRCode(desc, money, userIcon);
                }
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
