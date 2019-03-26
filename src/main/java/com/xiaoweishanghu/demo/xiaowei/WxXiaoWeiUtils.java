package com.xiaoweishanghu.demo.xiaowei;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.Consts;
import org.apache.http.ssl.SSLContexts;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 小微商户工具
 * @author luckxiong
 * @date 2019年3月13日
 */
@Slf4j
public class WxXiaoWeiUtils {

    private static final String TRANSFORMATION_PKCS1Paddiing = "RSA/ECB/PKCS1Padding";
    private static final String CIPHER_PROVIDER = "SunJCE";
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final String AES_KEY = "49b4cb9335654e2680bbc9dac4f59f9c"; // APIv3密钥

    /**
     * 获得敏感信息加密的秘钥
     * @param aad
     * @param iv
     * @param cipherText
     * @return
     * @throws Exception
     */
    public static String aesgcmDecrypt(String aad, String iv, String cipherText) throws Exception {
        final Cipher cipher = Cipher.getInstance(ALGORITHM, "SunJCE");
        SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(aad.getBytes());
        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
    }
    /**
     * 删除临时文件
     *
     * @param files
     */
    public static void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
    /**
     * 获取证书内容
     * @param certPath 证书地址
     * @param mchId 商户号
     * @return
     */
    public static SSLContext getSSLContext(String certPath, String mchId) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(certPath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] partnerId2charArray = mchId.toCharArray();
            keystore.load(inputStream, partnerId2charArray);
            return SSLContexts.custom().loadKeyMaterial(keystore, partnerId2charArray).build();
        } catch (Exception var9) {
            throw new RuntimeException("证书文件有问题，请核实！", var9);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     *
     * <pre>
     *     对敏感内容（入参Content）加密，其中PUBLIC_KEY_FILENAME为存放平台证书的路径，平台证书文件存放明文平台证书内容，
     *     且为pem格式的平台证书（平台证书的获取方式参照平台证书及序列号获取接口，
     *     通过此接口得到的参数certificates包含了加密的平台证书内容ciphertext，
     *     然后根据接口文档中平台证书解密指引，最终得到明文平台证书内容）
     * </pre>
     *
     * @param Content
     * @param publicKeyPath
     * @return
     * @throws Exception
     */

    public static String rsaEncrypt(String Content,String publicKeyPath) throws Exception {
        if (null == Content) return null;
        final byte[] PublicKeyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
        X509Certificate certificate = X509Certificate.getInstance(PublicKeyBytes);
        PublicKey publicKey = certificate.getPublicKey();
        byte[] bytes = encryptPkcs1padding(publicKey, Content.getBytes(Consts.UTF_8));
        return encodeBase64(bytes);
    }
    private static byte[] encryptPkcs1padding(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_PKCS1Paddiing, CIPHER_PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }
    private static String encodeBase64(byte[] bytes) throws Exception {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * byte[]转换成临时文件
     * @param bs
     * @return
     * @throws IOException
     */
    public static File byteArr2File(byte[] bs) throws IOException {
        File tempFile = File.createTempFile(UUID.randomUUID().toString()+atomicInteger.incrementAndGet(), ".jpg");
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
        bufferedOutput.write(bs);
        return tempFile;
    }

}
