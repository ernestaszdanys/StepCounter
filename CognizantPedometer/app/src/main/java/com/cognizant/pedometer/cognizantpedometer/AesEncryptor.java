package com.cognizant.pedometer.cognizantpedometer;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by ERZD01 on 2016-10-25.
 */

class AesEncryptor extends CryptLib {

    AesEncryptor() throws NoSuchAlgorithmException, NoSuchPaddingException {
    }

    String encrypt(String value) {
        String key;
        try {
            int shaLength = 32;
            String password = "CognizantBuildStuff2016.";
            String iv = "";

            key = SHA256(password, shaLength);
            return super.encrypt(value, key, iv);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }


}
