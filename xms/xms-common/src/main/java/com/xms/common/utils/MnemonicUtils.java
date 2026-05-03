package com.xms.common.utils;

import io.github.novacrypto.bip39.MnemonicGenerator;
import io.github.novacrypto.bip39.Words;
import io.github.novacrypto.bip39.wordlists.English;
import io.github.novacrypto.bip39.MnemonicValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;

/**
 * 助记词工具类 - 用于生成和验证 BIP39 标准的助记词
 * 
 * @author ujoin
 */
@Slf4j
public class MnemonicUtils {
    
    /**
     * 生成12个单词的助记词
     *
     * @return 助记词字符串，单词间用空格分隔
     */
    public static String generateMnemonic() {
        StringBuilder sb = new StringBuilder();
        byte[] entropy = new byte[Words.TWELVE.byteLength()];
        new SecureRandom().nextBytes(entropy);
        
        try {
            new MnemonicGenerator(English.INSTANCE).createMnemonic(entropy, sb::append);
            return sb.toString();
        } catch (Exception e) {
            log.error("Generate mnemonic error", e);
            return null;
        }
    }
    
    /**
     * 验证助记词是否有效
     *
     * @param mnemonic 助记词字符串，单词间用空格分隔
     * @return 是否有效
     */
    public static boolean validateMnemonic(String mnemonic) {
        if (StringUtils.isBlank(mnemonic)) {
            return false;
        }
        
        try {
            MnemonicValidator.ofWordList(English.INSTANCE).validate(mnemonic);
            return true;
        } catch (Exception e) {
            log.error("Validate mnemonic error", e);
            return false;
        }
    }
} 