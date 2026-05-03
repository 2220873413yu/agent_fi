package com.xms.common.utils.rsa;

import com.alibaba.fastjson2.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;


public class AESUtil {

	//瀵嗛挜 (闇�瑕佸墠绔拰鍚庣淇濇寔涓�鑷�)
    public static final String KEY = "QWGD284985G4MYJM";

    //绠楁硶
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";

    /**
     * 灏哹yte[]杞负鍚勭杩涘埗鐨勫瓧绗︿覆
     * @param bytes byte[]
     * @param radix 鍙互杞崲杩涘埗鐨勮寖鍥达紝浠嶤haracter.MIN_RADIX鍒癈haracter.MAX_RADIX锛岃秴鍑鸿寖鍥村悗鍙樹负10杩涘埗
     * @return 杞崲鍚庣殑瀛楃涓�
     */
    public static String binary(byte[] bytes, int radix){
        return new BigInteger(1, bytes).toString(radix);// 杩欓噷鐨�1浠ｈ〃姝ｆ暟
    }

    /**
     * base 64 encode
     * @param bytes 寰呯紪鐮佺殑byte[]
     * @return 缂栫爜鍚庣殑base 64 code
     */
    public static String base64Encode(byte[] bytes){
        return Base64.encode(bytes);
    }

    /**
     * base 64 decode
     * @param base64Code 寰呰В鐮佺殑base 64 code
     * @return 瑙ｇ爜鍚庣殑byte[]
     * @throws Exception
     */
    public static byte[] base64Decode(String base64Code) throws Exception{
        return StringUtil.isEmpty(base64Code) ? null : Base64.decode(base64Code);
    }


    /**
     * AES鍔犲瘑
     * @param content 寰呭姞瀵嗙殑鍐呭
     * @param encryptKey 鍔犲瘑瀵嗛挜
     * @return 鍔犲瘑鍚庣殑byte[]
     * @throws Exception
     */
    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));

        return cipher.doFinal(content.getBytes("utf-8"));
    }


    /**
     * AES鍔犲瘑涓篵ase 64 code
     * @param content 寰呭姞瀵嗙殑鍐呭
     * @param encryptKey 鍔犲瘑瀵嗛挜
     * @return 鍔犲瘑鍚庣殑base 64 code
     * @throws Exception
     */
    public static String aesEncrypt(String content, String encryptKey) throws Exception {
        return base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    /**
     * AES瑙ｅ瘑
     * @param encryptBytes 寰呰В瀵嗙殑byte[]
     * @param decryptKey 瑙ｅ瘑瀵嗛挜
     * @return 瑙ｅ瘑鍚庣殑String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);

        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }

    /**
     * 灏哹ase 64 code AES瑙ｅ瘑
     * @param encryptStr 寰呰В瀵嗙殑base 64 code
     * @param decryptKey 瑙ｅ瘑瀵嗛挜
     * @return 瑙ｅ瘑鍚庣殑string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr, String decryptKey) throws Exception {
        return StringUtil.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr), decryptKey);
    }

    /**
     * 娴嬭瘯
     */
    public static void main(String[] args) throws Exception {
    	JSONObject param = new JSONObject();
    	//缁х画鏀粯
//    	param.put("token", "637|L92WW16GCE2EF2WUDK5UYSR7PVSXE8CV");
//    	param.put("order_id", "83");
//    	param.put("pay_type", "03");
//    	param.put("pay_password", MD5Utils.MD5Encode("123456"));

    	param.put("token", "638|9GTJKZENVS8222XH5F2OQVXD27HAEV3H");



        String key = KEY;
        String paramKey = AESUtil.aesEncrypt(param.toJSONString(),key);
        System.out.println("把key加密(前端传入的key)："+paramKey);

        String aesEncrypt2 = Base64.encode(RSAUtilApp.encryptByPublicKey(key.getBytes(), SysSecurityKeyConstant.publicKey_app));
        System.out.println("给前端的公钥:"+aesEncrypt2);
//        String requestBody=param.toJSONString();

        //加密
//        System.out.println("加密后的数据："+s);
        System.out.println("============");

        byte[] hexStringToBytes = Base64.decode(aesEncrypt2);
        byte[] decryptByPrivateKey = RSAUtilApp.decryptByPrivateKey(hexStringToBytes,SysSecurityKeyConstant.privateKey_app);
        System.out.println("解密KEY值为："+new String(decryptByPrivateKey, "utf-8"));
       String context = AESUtil.aesDecrypt(paramKey, new String(decryptByPrivateKey, "utf-8"));
        System.out.println("解密请求参数："+ context);


    }

}
