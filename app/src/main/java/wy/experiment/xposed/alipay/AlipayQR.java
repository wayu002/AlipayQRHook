package wy.experiment.xposed.alipay;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import wy.experiment.xposed.ICracker;
import wy.experiment.xposed.util.QRTool;
import wy.experiment.xposed.util.XPConstant;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by wy on 2018/11/13.
 */

public class AlipayQR {
    private static final String TAG = "AlipayQR";
    private ClassLoader mLoader;
    private Object mCollectMoneyRpc;
    private QRCodeGenerateCallback mCallback;

    public interface QRCodeGenerateCallback {
        void codeGenerated(boolean success, String path);
    }

    public AlipayQR(QRCodeGenerateCallback callback){
        mCallback = callback;
    }

    public void handleLoadPackage(ClassLoader loader) throws Throwable {
        mLoader = loader;
        hookAppContext();
    }

    private void hookAppContext(){
        try {
            Log.d(TAG, "hookAppContext");
            final Class<?> launcherClazz = mLoader.loadClass(XPConstant.LAUNCHER_AGENT_CLASS);
            findAndHookMethod(launcherClazz, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Log.d(TAG, "LauncherApplicationAgent init()");
                    Object launcherInstance = param.thisObject;
                    Field applicationContext = launcherClazz.getDeclaredField("mMicroApplicationContext");
                    applicationContext.setAccessible(true);
                    Object microApplicationContext = applicationContext.get(launcherInstance);
                    Log.d(TAG, "get mMicroApplicationContext: " + microApplicationContext);
                    hookRpcService(microApplicationContext);
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void hookRpcService(Object microApplicationContext){
        Log.d(TAG, "hookQRMoneyRpcService");
        try {
            Class<?> applicationClazz = mLoader.loadClass(XPConstant.CONTEXT_IMPL_CLASS);
            Class<?> collectMoneyRpcClazz = mLoader.loadClass(XPConstant.MONEY_RPC_CLASS);
            Method getRpcMethod = applicationClazz.getMethod("findServiceByInterface", String.class);
            getRpcMethod.setAccessible(true);
            Log.d(TAG, "before invoke");
            Object rpcServiceInstance = getRpcMethod.invoke(microApplicationContext, XPConstant.RPC_SERVICE_CLASS);
            Log.d(TAG, "after invoke");
            Method getCollectMoneyMethod = rpcServiceInstance.getClass().getDeclaredMethod("getRpcProxy", Class.class);
            getCollectMoneyMethod.setAccessible(true);
            mCollectMoneyRpc = getCollectMoneyMethod.invoke(rpcServiceInstance, collectMoneyRpcClazz);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }


    public void generateQRCode(Bundle data){
        Log.d(TAG, "generateQRCode: ");
        if(mCollectMoneyRpc == null){
            notifyFailed();
            return;
        }
        String des = data.getString(XPConstant.QR_DES, null);
        float money = data.getFloat(XPConstant.QR_MONEY, 0f);
        Bitmap iconBmp = data.getParcelable(XPConstant.QR_ICON);
        if(TextUtils.isEmpty(des) || money == 0f || iconBmp == null){
            notifyFailed();
        }else{
            asyncSetQRMoney(des, money, iconBmp);
        }
    }

    private void asyncSetQRMoney(final String des, float money, Bitmap icon){
        if(mCollectMoneyRpc == null){
            notifyFailed();
            return;
        }
        Log.d(TAG, "asyncSetQRMoney");
        String consultReq = "com.alipay.transferprod.rpc.req.ConsultSetAmountReq";
        String consultRes = "com.alipay.transferprod.rpc.result.ConsultSetAmountRes";
        Observable.timer(0, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .map(aLong -> {
                    // ConsultSetAmountReq
                    Class<?> consultReqClazz = mLoader.loadClass(consultReq);
                    Object consultReqInstance = consultReqClazz.newInstance();
                    // Field amount
                    Field amountField = consultReqClazz.getField("amount");
                    amountField.set(consultReqInstance, "" + money);
                    // Field desc
                    Field descField = consultReqClazz.getField("desc");
                    descField.set(consultReqInstance, des);

                    // Method consultSetAmount
                    Method method = mCollectMoneyRpc.getClass().getMethod("consultSetAmount", consultReqClazz);
                    method.setAccessible(true);
                    Object resInstance = method.invoke(mCollectMoneyRpc, consultReqInstance);

                    // ConsultSetAmountRes
                    Class<?> consultResClazz = mLoader.loadClass(consultRes);
                    Field codeIdField = consultResClazz.getDeclaredField("codeId");
                    codeIdField.setAccessible(true);
                    String codeId = (String) codeIdField.get(resInstance);
                    Field printQrCodeUrlField = consultResClazz.getDeclaredField("printQrCodeUrl");
                    String printQrCodeUrl = (String) printQrCodeUrlField.get(resInstance);
                    Field qrCodeUrlField = consultResClazz.getDeclaredField("qrCodeUrl");
                    String qrCodeUrl = (String) qrCodeUrlField.get(resInstance);
                    Log.d(TAG, String.format("RPC request collect money, codeId: %s, printQrCodeUrl: %s, qrCodeUrl: %s",
                            codeId, printQrCodeUrl, qrCodeUrl));
                    return qrCodeUrl;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(codeUrl -> {
                    Bitmap qrImage = createQRBitmap(icon, codeUrl);
                    postProcessQRImage(qrImage);
                }, throwable -> Log.e(TAG, throwable.getMessage()));
    }

    private void postProcessQRImage(Bitmap qrImage) {
        if(qrImage == null){
            notifyFailed();
            return;
        }
        String fileName = "" + System.currentTimeMillis() + ".jpeg";
        String filePath = saveImage(qrImage, fileName);
        qrImage.recycle();
        notifySuccess(filePath);
    }

    private Bitmap createQRBitmap(Bitmap bitmap, String qrCodeUrl){
        if(TextUtils.isEmpty(qrCodeUrl)) {
            return null;
        }
        final ClassLoader loader = mLoader;
        try {
            Class<?> zXingClazz = loader.loadClass(XPConstant.ZXING_HELPER_CLASS);
            Class<?> barcodeClazz = loader.loadClass(XPConstant.BARCODE_FORMAT_ENUM);
            Class<?> correctionClazz = loader.loadClass(XPConstant.ERROR_CORRECTION_ENUM);
            if(zXingClazz != null && barcodeClazz != null && correctionClazz != null){
                Method method = zXingClazz.getDeclaredMethod("a", String.class, barcodeClazz, int.class, int.class, int.class,
                        correctionClazz, Bitmap.class, int.class, String.class, boolean.class);
                method.setAccessible(true);
                int width = QRTool.instance().getQRCodeWidth();
                Log.d(TAG, "QR width: " + width);
                Bitmap result =  (Bitmap) method.invoke(null, qrCodeUrl, barcodeClazz.getEnumConstants()[1],
                        0, width, width, correctionClazz.getEnumConstants()[3], getBitmap(bitmap), -16777216, null, false);
                if(result == null){
                    Log.e(TAG, "Cannot generate code");
                    return null;
                }
                Log.d(TAG, "invoke get bitmap: " + result);
                return convertBitmap(result);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private String saveImage(Bitmap bmp, String fileName) {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xpcracker");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 40, fos);
            fos.flush();
            fos.close();
            Log.d(TAG, "success save image: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private Bitmap convertBitmap(Bitmap bmp){
        Bitmap outBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, 0, 0, null);
        bmp.recycle();
        return outBmp;
    }

    private Bitmap getBitmap(Bitmap bitmap){
        int min = Math.min(bitmap.getWidth(), bitmap.getHeight());
        return createRoundBitmap(bitmap, (int) (((double) (min * 10)) / 114.0d), (int) (((double) (min * 4)) / 114.0d));
    }

    private Bitmap createRoundBitmap(Bitmap bitmap, int i, int i2){
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawRoundRect(new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())), (float) i, (float) i, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
            int i3 = i2 << 1;
            Bitmap createBitmap2 = Bitmap.createBitmap(createBitmap.getWidth() + i3, createBitmap.getHeight() + i3, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = new Canvas(createBitmap2);
            Paint paint2 = new Paint();
            paint2.setColor(-1);
            paint2.setStyle(Paint.Style.STROKE);
            paint2.setStrokeWidth((float) i3);
            paint2.setAntiAlias(true);
            canvas2.drawRoundRect(new RectF(new Rect(i3 / 2, i3 / 2, createBitmap2.getWidth() - (i3 / 2),
                    createBitmap2.getHeight() - (i3 / 2))), (float) i, (float) i, paint2);
            canvas2.drawBitmap(createBitmap, (float) (i3 / 2), (float) (i3 / 2), null);
            return createBitmap2;
        } catch (Exception e) {
            Log.e("BitmapUtil", "generate rounded corner bitmap failed.");
            return bitmap;
        }
    }

    private void notifyFailed(){
        if(mCallback != null){
            mCallback.codeGenerated(false, null);
        }
    }

    private void notifySuccess(String path){
        if(mCallback != null){
            mCallback.codeGenerated(true, path);
        }
    }
}
