[toc]

# 微信小微商户申请入驻API java demo，经测试已成功开通，欢迎修改补充，如果对你有帮助，欢迎star

##  申请入驻API首先要升级微信平台升级API证书
```
微信官方地址：http://kf.qq.com/faq/180824BrQnQB180824m6v2yA.html
```

## API证书满足要求后，满足下面的条件，下面代码都已在测试用例中

 - 获取平台证书序列号
 - 图片上传接口
 - 敏感信息加密
 
 
## 测试用例
```$xslt
com.xiaoweishanghu.demo.DemoApplicationTests
getCertFicates //获取平台证书序列号
uploadWxPic //图片上传接口
getPem //获取敏感信息加密公钥
applyment //调用申请入驻API
```
