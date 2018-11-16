package wy.experiment.xposed.wechat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import wy.experiment.xposed.ICracker;
import wy.experiment.xposed.IQR;
import wy.experiment.xposed.MessengerHandler;
import wy.experiment.xposed.QRCodeGenerateCallback;
import wy.experiment.xposed.alipay.AlipayCracker;
import wy.experiment.xposed.util.QRTool;
import wy.experiment.xposed.util.XPConstant;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by wy on 2018/11/15.
 */

public class WeChatCracker implements ICracker, QRCodeGenerateCallback {
    private static final String TAG = "WeChatCracker";
    private static WeChatCracker _instance;
    private boolean mIsLoaded;
    private Context mWeChatBaseContext;
    private IQR mWeChatQR;
    private Messenger mService;
    private Messenger mReplyMessenger;

    public static WeChatCracker instance() {
        if (_instance == null) {
            synchronized (WeChatCracker.class) {
                if (_instance == null) {
                    _instance = new WeChatCracker();
                }
            }
        }
        return _instance;
    }

    private WeChatCracker(){
        mReplyMessenger = new Messenger(new MessengerHandler(this));
    }

    @Override
    public IQR getQR() {
        return mWeChatQR;
    }

    @Override
    public void handleLoadPackage(ClassLoader loader) throws Throwable {
        if(isReadyForUse()){
            return;
        }
        mIsLoaded = true;

        hookContext(loader);
    }

    @Override
    public boolean isReadyForUse() {
        return mIsLoaded;
    }

    @Override
    public void qrCodeGenerated(boolean success, String path) {
        Log.d(TAG, String.format("qrCodeGenerated, success: %s, path: %s", success, path));
        QRTool.instance().sendQRComplete(success, path, mService);
    }

    private void hookContext(ClassLoader loader){
        try {
            findAndHookMethod(XPConstant.WE_APP_CLAZZ, loader, "attachBaseContext", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    mWeChatBaseContext = (Context) param.args[0];
                    Log.d(TAG, "attachBaseContext: " + mWeChatBaseContext.getClassLoader());
                    if(mWeChatQR != null){
                        return;
                    }
                    mWeChatQR = new WeChatQR(mWeChatBaseContext, WeChatCracker.this);
                    mWeChatQR.handleLoadPackage(loader);
                    connectToServer();
                }
            });
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectToServer() {
        try {
            boolean connectSuccess;
            Intent service = new Intent();
            service.setClassName("wy.experiment.xposed", "wy.experiment.xposed.service.CommandService");

            // bind service
            connectSuccess = mWeChatBaseContext.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bind service: " + connectSuccess);
            if(!connectSuccess){
                mWeChatBaseContext.unbindService(mConnection);
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
            Message msg = Message.obtain(null, XPConstant.WE_JOIN);
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
}
