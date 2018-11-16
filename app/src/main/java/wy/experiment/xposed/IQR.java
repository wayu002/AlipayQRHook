package wy.experiment.xposed;

import android.graphics.Bitmap;

/**
 * Created by wy on 2018/11/16.
 */

public interface IQR {
    void handleLoadPackage(ClassLoader loader);
    void generateQRCode(String desc, float money, Bitmap userIcon);
}
