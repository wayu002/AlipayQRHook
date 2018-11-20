[English](https://github.com/wayu002/AlipayQRHook/blob/master/ENGLISH.md)

AlipayQRHook
------

[破解过程可以参考，支付宝的](https://www.52pojie.cn/thread-821871-1-1.html)
[微信6.6.7的](https://www.52pojie.cn/forum.php?mod=viewthread&tid=823709&page=1#pid22661279)

支付宝目前已经对每日最高生成次数做了限制（20次），所以想通过这种方法做不好的事情的小伙伴还是歇歇吧，哈哈哈
-------


说明
------
这个工程是一个基于XPosed或VirtualXPosed的插件项目，用于自动生成支付宝或者微信的付款二维码，可以自定义金额以及备注信息，项目中去除了hook用户登录信息的功能，不会泄露任何支付宝用户信息，只能作为学习目的使用。

警告
------
这是一个已学习为目的的工程，请不要擅自用于商业使用，产生的问题概不负责！！！

演示
------
![img](https://github.com/wayu002/AlipayQRHook/blob/master/record.gif)

注意
------
支付宝已支持最新版本
目前仅支持微信6.6.7版本

使用
------
1. 手机安装XPosed或VirtualXPosed.（推荐使用VirtualXPosed，手机免root，演示视频使用的VirtualXPosed）
2. 克隆这个项目，在AS中编译生成app-debug.apk。
3. 在VirtualXPosed中安装支付宝或者微信，然后安装app-debug.apk，具体使用方法可以参考VirtualXPosed工程。
4. 在VirtualXPosed插件管理中勾选此插件，然后重启VirtualXPosed。
5. 打开支付宝和微信。
6. 打开AlipayQRHook，在操作界面中输入备注信息和金额，点击生成二维码。
7. 如果一切正常，会弹出提示，告诉二维码生成位置。
8. 默认二维码生成路径：支付宝("/sdcard/xpcracker/Alipay")，微信("/sdcard/xpcracker/WeChat")。
