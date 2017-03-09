package com.wifiin.data;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.wifiin.data.exception.DataTransformerException;
import com.wifiin.util.security.RSA;


public abstract class RSAPrivateKeyTransformer<T> implements Transformer<T,byte[],byte[]>{
    public abstract String getKey(T t);
    @Override
    public byte[] encode(T t,byte[] p){
        try {
            return RSA.encryptByPrivateKey(p, getKey(t));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
                | NoSuchPaddingException | InvalidKeySpecException | IOException e) {
            throw new DataTransformerException(e);
        }
    }

    @Override
    public byte[] decode(T t,byte[] r, Class<byte[]> cls){
        try {
            return RSA.decryptByPrivateKey(r, getKey(t));
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
                | NoSuchPaddingException | InvalidKeySpecException | IOException e) {
            throw new DataTransformerException(e);
        }
    }

}
