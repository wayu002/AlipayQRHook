package wy.experiment.xposed.wechat;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import wy.experiment.xposed.IQR;
import wy.experiment.xposed.QRCodeGenerateCallback;
import wy.experiment.xposed.util.QRTool;
import wy.experiment.xposed.util.XPConstant;
import wy.experiment.xposed.wechat.entity.WeChatQRInfo;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by wy on 2018/11/16.
 */

public class WeChatQR implements IQR {
    private static final String TAG = "WeChatQR";
    private ClassLoader mLoader;
    private QRCodeGenerateCallback mCallback;
    private Class<?> mQrRequestClazz;
    private Context mContext;

    public WeChatQR(Context context, QRCodeGenerateCallback callback){
        mContext = context;
        mCallback = callback;
    }

    @Override
    public void handleLoadPackage(ClassLoader loader) {
        try {
            mLoader = loader;
            mQrRequestClazz = loader.loadClass(XPConstant.WE_NET_REQ_CLASS);
            findAndHookMethod(mQrRequestClazz, "a", int.class, String.class, JSONObject.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Log.d(TAG, "reqClass: " + param.args[2]);
                            JSONObject jsonObject = (JSONObject) param.args[2];
                            WeChatQRInfo info = new Gson().fromJson(jsonObject.toString(), WeChatQRInfo.class);
                            if(info.returncode != 0){
                                notifyQRFailed();
                                return;
                            }
                            Class<?> qClazz = loader.loadClass(XPConstant.WE_INFO_CLAZZ);
                            Method GFMethod = qClazz.getDeclaredMethod("GF");
                            GFMethod.setAccessible(true);
                            String codeInfo = (String) GFMethod.invoke(null);
                            Class<?> bClazz = loader.loadClass(XPConstant.WE_QR_BMP_CLAZZ);
                            Class<?> toolClazz = loader.loadClass(XPConstant.WE_TOOL_CLAZZ);
                            Method aMethod = bClazz.getMethod("a", Context.class, String.class, String.class, int.class,
                                    String.class, toolClazz, boolean.class);
                            aMethod.setAccessible(true);
                            Bitmap qrCode = (Bitmap) aMethod.invoke(null, mContext, info.pay_url, codeInfo, 0, null, null, false);
                            if(qrCode == null){
                                Log.e(TAG, "generate qr bitmap failed!");
                                notifyQRFailed();
                                return;
                            }
                            String path = QRTool.instance().saveImage(qrCode, String.valueOf(System.currentTimeMillis()) + ".png",
                                    "WeChat", 1);
                            if(TextUtils.isEmpty(path)){
                                notifyQRFailed();
                            }else{
                                notifyQRSuccess(path);
                            }
                        }
                    });
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            notifyQRFailed();
        }
    }

    @Override
    public void generateQRCode(String desc, float money, Bitmap userIcon) {
        try {
            if(mQrRequestClazz == null || TextUtils.isEmpty(desc) || money == 0f){
                notifyQRFailed();
                return;
            }
            Constructor constructor = mQrRequestClazz.getConstructor(double.class, String.class, String.class);
            Object reqInstance = constructor.newInstance(money, "1", desc);
            Class<?> gClazz = mLoader.loadClass(XPConstant.WE_G_CLAZZ);
            Method ehMethod = gClazz.getDeclaredMethod("Eh");
            ehMethod.setAccessible(true);
            Object ehInstance = ehMethod.invoke(null);
            Field field = ehInstance.getClass().getDeclaredField("dpP");
            field.setAccessible(true);
            Object dpP = field.get(ehInstance);
            Class<?> lClazz = mLoader.loadClass(XPConstant.WE_L_CLAZZ);
            Method aMethod = dpP.getClass().getDeclaredMethod("a", lClazz, int.class);
            aMethod.setAccessible(true);
            aMethod.invoke(dpP, reqInstance, 0);
            Log.d(TAG, "we send the request");
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            notifyQRFailed();
        }
    }

    private void notifyQRSuccess(String path){
        if(mCallback != null){
            mCallback.qrCodeGenerated(true, path);
        }
    }

    private void notifyQRFailed(){
        if(mCallback != null){
            mCallback.qrCodeGenerated(false, null);
        }
    }
}
