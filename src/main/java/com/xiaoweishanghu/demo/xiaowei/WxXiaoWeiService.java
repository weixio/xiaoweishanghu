package com.xiaoweishanghu.demo.xiaowei;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.util.TextUtils.isBlank;

/**
 * @Auther: luckxiong
 * @Date: 2019-03-25 16:49
 * @Description:
 */
@Slf4j
@Service
public class WxXiaoWeiService {
    /**
     * 申请地址
     */
    @Value("${micromerchant.platform_certficates_get_url}")
    private String PLATFORM_CERTFICATES_GET_URL;
    /**
     * 申请入驻
     */
    @Value("${micromerchant.xiaowei_submit}")
    private String XIAOWEI_SUBMIT;
    /**
     * 获取申请状态
     */
    @Value("${micromerchant.xiaowei_getstate}")
    private String XIAOWEI_GETSTATE;

    /**
     * 微信图片上传地址
     */
    @Value("${micromerchant.path_wx_picture_update}")
    private String PATH_WX_PICTURE_UPDATE;
    /**
     * 商户号id
     */
    @Value("${micromerchant.mch_id}")
    public String MCH_ID;
    /**
     * 商户秘钥
     */
    @Value("${micromerchant.mch_key}")
    public String MCH_KEY;
    /**
     * 证书地址
     */
    @Value("${micromerchant.cert_path}")
    private String CERT_PATH;
    /**
     * 公钥pem文件
     */
    @Value("${micromerchant.public_key_filename}")
    public String PUBLIC_KEY_FILENAME;

    /**
     * 申请入驻API请求调用
     *
     * @param params
     * @return
     * @throws IOException
     */
    public String xwApplyApiHttpsSend(Map<String, String> params) throws Exception {
        String responseEntity = httpsSend(params,XIAOWEI_SUBMIT);
        log.info("微信返回的报文：{}", responseEntity);
        return parseResApplyApi(responseEntity);
    }
    /**
     * 解析申请API返回参数，返回 商户申请单号
     *
     * @param responseEntity
     * @return
     * @throws Exception
     */
    private String parseResApplyApi(String responseEntity) throws Exception {
        Map<String, String> resMap = WXPayUtil.xmlToMap(responseEntity);
        String return_code = resMap.get(WxXiaoWeiConstants.RETURN_CODE);
        if (null == return_code) {
            return "错误";
        }
        if (WxXiaoWeiConstants.FAIL.equals(return_code)) {
            return "错误";
        }
        String result_code = resMap.get(WxXiaoWeiConstants.RESULT_CODE);
        if (null == result_code) {
            return "错误";
        }
        if (WxXiaoWeiConstants.FAIL.equals(result_code)) {
            return "错误";
        }
        String applyment_id = resMap.get(WxXiaoWeiConstants.APPLYMENT_ID);
        if (null == applyment_id) {
            return "错误";
        }
        return applyment_id;
    }

    /**
     * 请求调用
     *
     * @param params
     * @return
     * @throws Exception
     */
    private String httpsSend(Map<String, String> params,String Url) throws Exception {
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(Url));
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_XML.getMimeType());
        String stringbyXml = WXPayUtil.mapToXml(params);
        StringEntity stringEntity = new StringEntity(stringbyXml,Consts.UTF_8);
        httpPost.setEntity(stringEntity);
        CloseableHttpClient client = HttpClients.custom().setSSLContext(WxXiaoWeiUtils.getSSLContext(CERT_PATH, MCH_ID)).build();
        HttpResponse httpResponse = client.execute(httpPost);
        return EntityUtils.toString(httpResponse.getEntity());
    }
    /**
     * 平台证书序列号 获取
     */
    public String getCertFicates() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(PLATFORM_CERTFICATES_GET_URL);
        Map<String, String> param = new HashMap<>(4);
        param.put(WxXiaoWeiConstants.MCH_ID, MCH_ID);
        param.put(WxXiaoWeiConstants.NONCE_STR, WXPayUtil.generateNonceStr());
        param.put(WxXiaoWeiConstants.SIGN_TYPE, WXPayConstants.HMACSHA256);
        try {
            param.put(WxXiaoWeiConstants.SIGN, WXPayUtil.generateSignature(param, MCH_KEY, WXPayConstants.SignType.HMACSHA256));
            String stringbyXml = WXPayUtil.mapToXml(param);
            StringEntity entity = new StringEntity(stringbyXml);
            httpPost.setEntity(entity);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_XML.getMimeType());
            HttpResponse httpResponse = httpClient.execute(httpPost);
            log.info("获取平台证书响应 msg={}", httpResponse);
            if (httpResponse == null || HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                return "错误";
            }
            String responseEntity = EntityUtils.toString(httpResponse.getEntity());
            log.info("平台证书序列号返回的xml:{}",responseEntity);
            //这里返回最后的 平台证书序列号
            return parseDerialNo(responseEntity);
        } catch (UnsupportedEncodingException e) {
            log.error("StringEntity 解析错误！ e={}", e);
        } catch (IOException e) {
            log.error("执行httpclient请求平台证书序号错误 e={}", e);
        } catch (Exception e) {
            log.error("转换sign错误！ e={}", e);
        }
        return "错误";
    }

    /**
     * 解析微信接口返回参数，获得平台证书序号
     *
     * @param responseEntity
     * @return
     */
    private String parseDerialNo(String responseEntity) {
        Map<String, String> resMap = null;
        try {
            resMap = WXPayUtil.xmlToMap(responseEntity);
        } catch (Exception e) {
            log.error("map解析错误! e={}", e);
        }
        if (null == resMap) {
            return "错误";
        }
        String certificates = resMap.get(WxXiaoWeiConstants.CERTIFICATES);
        if (isBlank(certificates)) {
            return "错误";
        }
        String serialNo = JSON.parseObject(certificates).getJSONArray(WxXiaoWeiConstants.DATA).getJSONObject(0).getString(WxXiaoWeiConstants.SERIAL_NO);
        if (isBlank(serialNo)) {
            return "错误";
        }
        return serialNo;
    }

    /**
     * 微信图文上传
     *
     * @param picPath
     * @return
     */
    public String uploadWxPicture(String picPath){
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(PATH_WX_PICTURE_UPDATE));
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.MULTIPART_FORM_DATA.getMimeType());
        CloseableHttpClient client = null;
        File picFile = new File(picPath);
        try {
            String hash = hash(picPath);
            Map<String, String> param = new HashMap<>(6);
            param.put(WxXiaoWeiConstants.MEDIA_HASH, hash);
            param.put(WxXiaoWeiConstants.MCH_ID, MCH_ID);
            param.put(WxXiaoWeiConstants.SIGN_TYPE, WXPayConstants.HMACSHA256);
            FileBody bin = new FileBody(picFile, ContentType.MULTIPART_FORM_DATA);
            HttpEntity build = MultipartEntityBuilder.create().setCharset(Consts.UTF_8)
                    .addTextBody(WxXiaoWeiConstants.MEDIA_HASH, hash)
                    .addTextBody(WxXiaoWeiConstants.MCH_ID, MCH_ID)
                    .addTextBody(WxXiaoWeiConstants.SIGN_TYPE, WXPayConstants.HMACSHA256)
                    .addTextBody(WxXiaoWeiConstants.SIGN, WXPayUtil.generateSignature(param, MCH_KEY, WXPayConstants.SignType.HMACSHA256))
                    .addPart(WxXiaoWeiConstants.MEDIA, bin)
                    .build();
            httpPost.setEntity(build);
            client = HttpClients.custom().setSSLContext(WxXiaoWeiUtils.getSSLContext(CERT_PATH, MCH_ID)).build();
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse == null || HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                return "失败";
            }
            String responseEntity = EntityUtils.toString(httpResponse.getEntity());
            log.info("调用微信图片上传接口返回的报文：{}", responseEntity);
            return parseMediaId(responseEntity);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public String hash(String picPath) throws Exception {
        FileInputStream fis = new FileInputStream(picPath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 100];
        int n;
        while ((n = bufferedInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        byte[] data = out.toByteArray();
        return DigestUtils.md5Hex(data);
    }
    /**
     * 获得图片media_id
     *
     * @param responseEntity
     * @return
     */
    private String parseMediaId(String responseEntity) {
        Map<String, String> resMap = null;
        try {
            resMap = WXPayUtil.xmlToMap(responseEntity);
        } catch (Exception e) {
            log.error("map解析错误! e={}", e);
        }
        if (null == resMap) {
            return "失败";
        }
        String media_id = resMap.get(WxXiaoWeiConstants.MEDIA_ID);
        if (isBlank(media_id)) {
            return "失败";
        }
        return media_id;
    }
}
