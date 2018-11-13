[中文说明](https://github.com/wayu002/AlipayQRHook/blob/master/CHINESE.md)

AlipayQRHook
-------

Introduction
-------
This project is a plug-in project based on XPosed or VirtualXPosed. It is used to automatically generate Alipay QR code. It can customize the amount and remark information. The function of hook user login information is removed from the project, and no Alipay user information will be revealed. Used for learning purposes.

Warning
------
This is a project that has been studied for purpose. Please do not use it for commercial use！！！

Usage
------
1. Install XPosed or VirtualXposed.
2. Clone and build this project, generate app-debug.apk.
3. Install Alipay and app-debug.apk in the XPosed or VirtualXposed.
4. Open Alipay, and navigate to "Collect" page and back to home. (In order to get information to generate QR code URL)
5. Open the AlipayQRHook app, complete the memo and money input, click button to generate QR code.
6. If everything is ok, app will pop up toast to remind you where the QR code is generated.
7. By default, the QR code is generate in "/sdcard/xpcracker".
