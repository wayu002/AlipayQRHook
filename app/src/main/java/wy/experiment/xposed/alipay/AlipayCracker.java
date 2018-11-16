package wy.experiment.xposed.alipay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.XC_MethodHook;
import wy.experiment.xposed.ICracker;
import wy.experiment.xposed.IQR;
import wy.experiment.xposed.MessengerHandler;
import wy.experiment.xposed.QRCodeGenerateCallback;
import wy.experiment.xposed.util.QRTool;
import wy.experiment.xposed.util.XPConstant;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by wy on 2018/11/13.
 */

public class AlipayCracker implements ICracker, QRCodeGenerateCallback {
    private static final String TAG = "AlipayCracker";
    private boolean mIsLoaded = false;
    private Messenger mService;
    private Messenger mReplyMessenger;
    private Context mAlipayContext;
    private ClassLoader mLoader;
    private static AlipayCracker _instance;
    private IQR mQRCode;

    public static AlipayCracker instance() {
        if (_instance == null) {
            synchronized (AlipayCracker.class) {
                if (_instance == null) {
                    _instance = new AlipayCracker();
                }
            }
        }
        return _instance;
    }

    private AlipayCracker(){
        mReplyMessenger = new Messenger(new MessengerHandler(this));
    }

    @Override
    public void handleLoadPackage(ClassLoader loader) throws Throwable {
        if (isReadyForUse()){
            return;
        }
        mIsLoaded = true;

        hookStartCommandService(loader);
    }

    @Override
    public boolean isReadyForUse() {
        return mIsLoaded;
    }

    @Override
    public IQR getQR() {
        return mQRCode;
    }

    private void hookStartCommandService(ClassLoader loader){
        findAndHookMethod(XPConstant.APPLICATION_CLASS, loader, "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "hookStartCommandService->afterHookedMethod: " + param.args[0]);
                mAlipayContext = (Context) param.args[0];
                QRTool.instance().init(mAlipayContext);
                mLoader = mAlipayContext.getClassLoader();
                mQRCode = new AlipayQR(AlipayCracker.this);
                mQRCode.handleLoadPackage(mLoader);
                connectToServer();
            }
        });
    }

    private void connectToServer() {
        try {
            boolean connectSuccess;
            Intent service = new Intent();
            service.setClassName("wy.experiment.xposed", "wy.experiment.xposed.service.CommandService");

            // bind service
            connectSuccess = mAlipayContext.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bind service: " + connectSuccess);
            if(!connectSuccess){
                mAlipayContext.unbindService(mConnection);
            }
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mService = new Messenger(service);
            Message msg = Message.obtain(null, XPConstant.ALI_JOIN);
            msg.replyTo = mReplyMessenger;
            try {
                mService.send(msg);
            } catch (Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    public void qrCodeGenerated(boolean success, String path) {
        QRTool.instance().sendQRComplete(success, path, mService);
    }
}
