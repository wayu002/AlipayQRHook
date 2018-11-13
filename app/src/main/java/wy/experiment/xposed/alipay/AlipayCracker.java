package wy.experiment.xposed.alipay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import wy.experiment.xposed.util.QRTool;
import wy.experiment.xposed.util.XPConstant;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by wy on 2018/11/13.
 */

public class AlipayCracker implements ICracker, AlipayQR.QRCodeGenerateCallback {
    private static final String TAG = "AlipayCracker";
    private boolean mIsLoaded = false;
    private Messenger mService;
    private Messenger mReplyMessenger;
    private Context mAlipayContext;
    private ClassLoader mLoader;
    private static AlipayCracker _instance;
    private AlipayQR mQRCode;

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
        mReplyMessenger = new Messenger(new AlipayMessengerHandler(this));
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
            // start service first
            mAlipayContext.startService(service);

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
    public void codeGenerated(boolean success, String path) {
        Message msg = Message.obtain(null, XPConstant.ALI_QR_COMPLETE);
        Bundle data = new Bundle();
        data.putBoolean(XPConstant.QR_SUCCESS, success);
        if(success){
            data.putString(XPConstant.QR_PATH, path);
        }
        msg.setData(data);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static class AlipayMessengerHandler extends Handler {
        private WeakReference<AlipayCracker> mCracker;

        AlipayMessengerHandler(AlipayCracker cracker){
            mCracker = new WeakReference<>(cracker);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mCracker.get() == null || !mCracker.get().isReadyForUse()){
                Log.e(TAG, "AlipayCracker is died");
                return;
            }
            switch (msg.what){
                case XPConstant.ALI_GENERATE_QR:
                    Log.d(TAG, "receive ALI_GENERATE_QR");
                    if(mCracker.get() != null && mCracker.get().mQRCode != null){
                        mCracker.get().mQRCode.generateQRCode(msg.getData());
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
