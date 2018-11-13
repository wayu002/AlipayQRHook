package wy.experiment.xposed;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import wy.experiment.xposed.alipay.AlipayCracker;
import wy.experiment.xposed.util.XPConstant;

/**
 * Created by wy on 2018/11/13.
 */

public class MainIntercept implements IXposedHookLoadPackage {
    private static final String TAG = "MainIntercept";
    private ICracker mCracker;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(XPConstant.ALI_PACKAGE)) {
            return;
        }

        if(lpparam.packageName.equals(XPConstant.ALI_PACKAGE) && lpparam.processName.equals(XPConstant.ALI_PACKAGE)){
            mCracker = AlipayCracker.instance();
        }

        Log.d(TAG, "handleLoadPackage: " + lpparam.classLoader + ", param: " + lpparam);
        Log.d(TAG, String.format("package: %s, process: %s", lpparam.packageName, lpparam.processName));

        if(mCracker != null && !mCracker.isReadyForUse()){
            mCracker.handleLoadPackage(lpparam.classLoader);
        }
    }
}
