AlipayQRHook
------

说明
------
这个工程是一个基于XPosed或VirtualXPosed的插件项目，用于自动生成支付宝二维码，可以自定义金额以及备注信息，项目中去除了hook用户登录信息的功能，不会泄露任何支付宝用户信息，只能作为学习目的使用。

警告
------
这是一个已学习为目的的工程，请不要擅自用于商业使用，产生的问题概不负责！！！

演示
------
![img](https://github.com/wayu002/AlipayQRHook/blob/master/record.gif)

使用
------
1. 手机安装XPosed或VirtualXPosed.（推荐使用VirtualXPosed，手机免root，演示视频使用的VirtualXPosed）
2. 克隆这个项目，在AS中编译生成app-debug.apk。
3. 在VirtualXPosed中安装支付宝和app-debug.apk，具体使用方法可以参考VirtualXPosed工程。
4. 在VirtualXPosed插件管理中勾选此插件，然后重启VirtualXPosed。
5. 打开支付宝，点击“收钱”进入该页面，然后点击返回，（这么做是为了获取生成二维码必要的信息，更直接的方法是hook用户登录，拿到登录信息，但是考虑到安全性， 这个项目去掉了hook登陆的操作。）
6. 打开AlipayQRHook，在操作界面中输入备注信息和金额，点击生成二维码。
7. 如果一切正常，会弹出提示，告诉二维码生成位置。
8. 默认二维码会生成在手机 "/sdcard/xpcracker"目录下，可以再次查看验证。
