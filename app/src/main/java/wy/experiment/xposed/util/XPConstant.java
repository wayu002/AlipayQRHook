package wy.experiment.xposed.util;

/**
 * Created by wy on 2018/11/13.
 */

public class XPConstant {
    private static final String TAG = "XPConstant";
    public static final String ALI_PACKAGE = "com.eg.android.AlipayGphone";
    public static final String APPLICATION_CLASS = "com.alipay.mobile.quinox.LauncherApplication";
    public static final String MONEY_RPC_CLASS = "com.alipay.transferprod.rpc.CollectMoneyRpc";
    public static final String ZXING_HELPER_CLASS = "com.alipay.android.phone.wallet.ZXingHelper";
    public static final String BARCODE_FORMAT_ENUM = "com.alipay.android.phone.wallet.minizxing.BarcodeFormat";
    public static final String ERROR_CORRECTION_ENUM = "com.alipay.android.phone.wallet.minizxing.ErrorCorrectionLevel";
    public static final String CONTEXT_IMPL_CLASS = "com.alipay.mobile.core.impl.MicroApplicationContextImpl";
    public static final String RPC_SERVICE_CLASS = "com.alipay.mobile.framework.service.common.RpcService";
    public static final String LAUNCHER_AGENT_CLASS = "com.alipay.mobile.framework.LauncherApplicationAgent";
    public static final String QR_ACTIVITY_CLASS = "com.alipay.mobile.payee.ui.PayeeQRActivity";
    public static final String SECURITY_CLASS = "com.alipay.android.phone.mobilesdk.storage.encryption.TaobaoSecurityEncryptor";

    public static final String QR_DES = "qr_des";
    public static final String QR_MONEY = "qr_money";
    public static final String QR_ICON = "qr_icon";
    public static final String QR_SUCCESS = "qr_success";
    public static final String QR_PATH = "qr_path";

    // message what code
    public static final int ALI_GENERATE_QR = 1000;
    public static final int ALI_JOIN = 1001;
    public static final int TEST_JOIN = 1002;
    public static final int ALI_QR_COMPLETE = 1003;
}
