package wy.experiment.xposed;

/**
 * Created by wy on 2018/11/16.
 */

public interface QRCodeGenerateCallback {
    void qrCodeGenerated(boolean success, String path);
}
