package com.example.padlockdemo.util;

import java.util.Formatter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {
    private static final byte[] key = {
            (byte)0x2b, (byte)0x7e, (byte)0x15, (byte)0x16, (byte)0x28, (byte)0xae, (byte)0xd2, (byte)0xa6,
            (byte)0xab, (byte)0xf7, (byte)0x15, (byte)0x88, (byte)0x09, (byte)0xcf, (byte)0x4f, (byte)0x3c
    };

    public static byte[] encrypt(byte[] data) {
        try {
            byte[] buffer = new byte[16];
            System.arraycopy(data, 0, buffer, 0, data.length);
            return encryptWithKey(key, buffer);
        } catch (Exception exception) {
            return null;
        }
    }

    public static byte[] decrypt(byte[] encrypted) {
        try {
            return decryptWithKey(key, encrypted);
        } catch (Exception exception) {
            return null;
        }
    }

    public static byte[] encryptWithKey(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(data);
        return encrypted;
    }

    public static byte[] decryptWithKey(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String byteToHexString(byte[] b) {
        int len = b.length;
        StringBuilder sb = new StringBuilder(b.length * (2 + 1));
        Formatter formatter = new Formatter(sb);

        for (int i = 0; i < len; i++) {
            if (i < len - 1)
                formatter.format("0x%02X:", b[i]);
            else
                formatter.format("0x%02X", b[i]);
        }
        formatter.close();

        return sb.toString();
    }
}
