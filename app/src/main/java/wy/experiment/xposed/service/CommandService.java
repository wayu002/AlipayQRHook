package wy.experiment.xposed.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import wy.experiment.xposed.util.XPConstant;

/**
 * Created by wy on 2018/11/13.
 */

public class CommandService extends Service {
    private static final String TAG = "CommandService";
    private Messenger mService = new Messenger(new MessengerHandler(this));

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mService.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate, pid: " + Process.myPid());
    }

    private static class MessengerHandler extends Handler {
        private WeakReference<CommandService> mParentService;
        private Messenger mAlipayClient;
        private Messenger mTestClient;

        public MessengerHandler(CommandService service) {
            mParentService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case XPConstant.ALI_JOIN:
                    alipayProcessJoin(msg.replyTo);
                    break;
                case XPConstant.TEST_JOIN:
                    testAppJoin(msg.replyTo);
                    break;
                case XPConstant.ALI_QR_COMPLETE:
                    alipayQRComplete(msg);
                    break;
                case XPConstant.ALI_GENERATE_QR:
                    alipayGenQR(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private void alipayGenQR(Message msg) {
            try {
                Message clone = Message.obtain();
                clone.copyFrom(msg);
                mAlipayClient.send(clone);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private void alipayQRComplete(Message msg) {
            try {
                Message clone = Message.obtain();
                clone.copyFrom(msg);
                mTestClient.send(clone);
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        private void testAppJoin(Messenger replyTo) {
            mTestClient = replyTo;
        }

        private void alipayProcessJoin(Messenger replyTo) {
            mAlipayClient = replyTo;
        }
    }
}
