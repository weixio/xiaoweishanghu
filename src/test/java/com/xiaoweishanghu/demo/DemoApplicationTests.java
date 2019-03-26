package com.xiaoweishanghu.demo;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.xiaoweishanghu.demo.xiaowei.WxXiaoWeiConstants;
import com.xiaoweishanghu.demo.xiaowei.WxXiaoWeiService;
import com.xiaoweishanghu.demo.xiaowei.WxXiaoWeiUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    private WxXiaoWeiService wxXiaoWeiService;

    /**
     * 获取平台证书序列号
     */
    @Test
    public void getCertFicates() {
        String certFicates = wxXiaoWeiService.getCertFicates();
        System.out.println(MessageFormat.format("获取的平台证书序列号: {0} ",certFicates));
    }

    /**
     * 微信图片上传
     */
    @Test
    public void uploadWxPic(){
        String media_id = wxXiaoWeiService.uploadWxPicture("C:\\Users\\Administrator\\Desktop\\pic1.png");//桌面的pic1.png图片地址
        System.out.println(MessageFormat.format("图片上传后返回的media_id: {0} ",media_id));
    }

    /**
     * 根据获取平台证书的方法机会的报文，填写 ‘associatedData’ ‘nonce’ ‘cipherText’得到的字符存到一个*.pem文件中，作为公钥，实现 ‘敏感信息加密’
     * {@link WxXiaoWeiService#getCertFicates()}
     * AesGcmExample类中的  AES_KEY  更改为你的APIv3密钥
     * 返回的公钥存放在 micromerchant.public_key_filename 参数所在的文件中，参与加密
     */
    @Test
    public void getPem(){
        final String associatedData = ""; // encrypt_certificate.associated_data
        final String nonce = ""; // encrypt_certificate.nonce
        final String cipherText = "";
        try {
            String wechatpayCert = WxXiaoWeiUtils.aesgcmDecrypt(associatedData, nonce, cipherText);
            System.out.println(wechatpayCert);//这里就是解密出来的公钥
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用小微商户申请入驻API
     */
    @Test
    public void applyment() throws Exception {
        Map<String, String> param = new HashMap<>(3);
        noNullPut(WxXiaoWeiConstants.VERSION, "3.0", param);
        //1 获取平台证书序列号
        noNullPut(WxXiaoWeiConstants.CERT_SN, wxXiaoWeiService.getCertFicates(), param);
        //商户号
        noNullPut(WxXiaoWeiConstants.MCH_ID, wxXiaoWeiService.MCH_ID, param);
        //随机字符串
        noNullPut(WxXiaoWeiConstants.NONCE_STR, WXPayUtil.generateNonceStr(), param);
        //签名类型
        noNullPut(WxXiaoWeiConstants.SIGN_TYPE, WXPayConstants.HMACSHA256, param);
        //业务申请编号
        noNullPut(WxXiaoWeiConstants.BUSINESS_CODE, "", param);
        //身份证人像面照片 需要图片接口
        noNullPut(WxXiaoWeiConstants.ID_CARD_COPY, "", param);
        //身份证国徽面照片  需要图片接口
        noNullPut(WxXiaoWeiConstants.ID_CARD_NATIONAL, "", param);
       //身份证有效期限
        noNullPut(WxXiaoWeiConstants.ID_CARD_VALID_TIME, "", param);
        //开户银行
        noNullPut(WxXiaoWeiConstants.ACCOUNT_BANK, "", param);
        //开户银行省市编码
        noNullPut(WxXiaoWeiConstants.BANK_ADDRESS_CODE, "", param);
        //开户银行全称（含支行）
        noNullPut(WxXiaoWeiConstants.BANK_NAME, "", param);
        //门店名称
        noNullPut(WxXiaoWeiConstants.STORE_NAME, "", param);
        //门店省市编码
        noNullPut(WxXiaoWeiConstants.STORE_ADDRESS_CODE, "", param);
       // 门店街道名称
        noNullPut(WxXiaoWeiConstants.STORE_STREET, "", param);
        //门店经度
        noNullPut(WxXiaoWeiConstants.STORE_LONGITUDE, "", param);
        //门店纬度
        noNullPut(WxXiaoWeiConstants.STORE_LATITUDE, "", param);
        //门店门口照片  需要图片接口
        noNullPut(WxXiaoWeiConstants.STORE_ENTRANCE_PIC,"", param);
        //店内环境照片  需要图片接口
        noNullPut(WxXiaoWeiConstants.INDOOR_PIC, "", param);
        //经营场地证明  需要图片接口
        noNullPut(WxXiaoWeiConstants.ADDRESS_CERTIFICATION, "", param);
        //商户简称
        noNullPut(WxXiaoWeiConstants.MERCHANT_SHORTNAME, "", param);
        //客服电话
        noNullPut(WxXiaoWeiConstants.SERVICE_PHONE, "", param);
        //售卖商品/提供服务描述
        noNullPut(WxXiaoWeiConstants.PRODUCT_DESC, "", param);
        //费率
        noNullPut(WxXiaoWeiConstants.RATE, "", param);
        //补充说明
        noNullPut(WxXiaoWeiConstants.BUSINESS_ADDITION_DESC, "", param);
        //补充材料  最多五张mediaId
        noNullPut(WxXiaoWeiConstants.BUSINESS_ADDITION_PICS, "", param);
        //联系人姓名  下面都是敏感信息加密
        noNullPut(WxXiaoWeiConstants.CONTACT, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
        //手机号码
        noNullPut(WxXiaoWeiConstants.CONTACT_PHONE, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
        //银行账号
        noNullPut(WxXiaoWeiConstants.ACCOUNT_NUMBER, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
//        联系邮箱
        noNullPut(WxXiaoWeiConstants.CONTACT_EMAIL, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
        //身份证姓名
        noNullPut(WxXiaoWeiConstants.ID_CARD_NAME, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
        //身份证号码
        noNullPut(WxXiaoWeiConstants.ID_CARD_NUMBER, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
        //开户名称
        noNullPut(WxXiaoWeiConstants.ACCOUNT_NAME, WxXiaoWeiUtils.rsaEncrypt("", wxXiaoWeiService.PUBLIC_KEY_FILENAME), param);
        String sign = WXPayUtil.generateSignature(param, wxXiaoWeiService.MCH_KEY, WXPayConstants.SignType.HMACSHA256);
        noNullPut(WxXiaoWeiConstants.SIGN, sign, param);
        String applyment_id = wxXiaoWeiService.xwApplyApiHttpsSend(param);
        System.out.println(MessageFormat.format("商户申请编号为: {0}，其他信息可以看返回的报文 ",applyment_id));
    }

    public void noNullPut(String key, String val, Map map) {
        if (isNotBlank(val)) {
            map.put(key, val);
        }
    }
}
