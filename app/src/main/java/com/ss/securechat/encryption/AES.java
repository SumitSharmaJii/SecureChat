package com.ss.securechat.encryption;

import android.util.Base64;
import android.widget.Toast;

import com.ss.securechat.Activities.ChatActivity;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {


    public static SecretKey getSecretKey() {
        SecretKey secretKey = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return secretKey;

    }


    public static String encrypt(byte[] plaintext, byte[] key) throws Exception {

        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] cipherText = Base64.encode(cipher.doFinal(plaintext),Base64.DEFAULT);
        return new String(cipherText);
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedText = cipher.doFinal( Base64.decode(cipherText,Base64.DEFAULT));
            return decryptedText;
        } catch (Exception e) {

            e.printStackTrace();

        }
        return null;
    }
}
