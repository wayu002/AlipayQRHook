package wy.experiment.xposed;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wy.experiment.xposed.util.XPConstant;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.qr_memo)
    EditText mEditMemo;
    @BindView(R.id.qr_money)
    EditText mEditeMoney;
    @BindView(R.id.gen_qr)
    Button mGenQR;
    private Messenger mService;
    private Bitmap mDefaultIcon;
    private Messenger mReplyMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mGenQR.setEnabled(false);
        Intent service = new Intent();
        service.setClassName(this, "wy.experiment.xposed.service.CommandService");
        bindService(service, mConnection, BIND_AUTO_CREATE);
        mDefaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_head);
        mReplyMessenger = new Messenger(new ClientMessengerHandler(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @OnClick(R.id.gen_qr)
    public void genQR(){
        if(TextUtils.isEmpty(mEditMemo.getText().toString())){
            Toast.makeText(this, "备注不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            float money = Float.valueOf(mEditeMoney.getText().toString());
            Bundle data = new Bundle();
            data.putString(XPConstant.QR_DES, mEditMemo.getText().toString());
            data.putFloat(XPConstant.QR_MONEY, money);
            data.putParcelable(XPConstant.QR_ICON, mDefaultIcon);
            Message msg = Message.obtain(null, XPConstant.ALI_GENERATE_QR);
            msg.setData(data);
            mService.send(msg);
        }catch (NumberFormatException e){
            Toast.makeText(this, "请输入正确金额", Toast.LENGTH_SHORT).show();
            mEditeMoney.setText("");
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mService = new Messenger(service);
            Message msg = Message.obtain(null, XPConstant.TEST_JOIN);
            try {
                msg.replyTo = mReplyMessenger;
                mService.send(msg);
                mGenQR.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mGenQR.setEnabled(false);
        }
    };

    private static class ClientMessengerHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        ClientMessengerHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mActivity.get() == null){
                return;
            }
            switch (msg.what){
                case XPConstant.ALI_QR_COMPLETE:
                    boolean success = msg.getData().getBoolean(XPConstant.QR_SUCCESS, false);
                    if(!success){
                        Toast.makeText(mActivity.get(), "生成二维码失败", Toast.LENGTH_SHORT).show();
                    }else{
                        String path = msg.getData().getString(XPConstant.QR_PATH, "");
                        Toast.makeText(mActivity.get(), "成功生成二维码，位置：" + path, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
}
