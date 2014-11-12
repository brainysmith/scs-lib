package com.identityblitz.scs.glue;

import com.identityblitz.scs.ConfigParameter;
import com.identityblitz.scs.service.ServiceProvider;
import com.identityblitz.scs.service.spi.CryptoException;
import com.identityblitz.scs.service.spi.CryptoTransformationService;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import static com.identityblitz.scs.LoggingUtils.getLogger;
import static com.identityblitz.scs.service.ServiceProvider.service;

/**
 * The basic cryptographic service implementing {@link com.identityblitz.scs.service.spi.CryptoTransformationService} interface.
 */
public class BasicCryptoService implements CryptoTransformationService {
    private final SecretKeySpec encKey;
    private final SecretKeySpec hmacKey;

    ThreadLocal<SecureRandom> secureRandom = new ThreadLocal<SecureRandom>() {
        @Override
        protected SecureRandom initialValue() {
            return new SecureRandom();
        }
    };

    ThreadLocal<Cipher> cipher = new ThreadLocal<Cipher>() {
        @Override
        protected Cipher initialValue() {
            final Cipher cph;
            try {
                cph = Cipher.getInstance("AES/CBC/PKCS5Padding");
                return cph;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
        }
    };

    ThreadLocal<Mac> mac = new ThreadLocal<Mac>() {
        @Override
        protected Mac initialValue() {
            try {
                final Mac hmac = Mac.getInstance("HmacSHA1");
                hmac.init(hmacKey);
                return hmac;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public BasicCryptoService() throws DecoderException {
        final String strEncKey = service().getConfiguration().getString(ConfigParameter.ENCODE_KEY.key());
        if(strEncKey == null) {
            getLogger().error("encoding key is undefined. To fix it is necessary to set " +
                    "configuration parameter [" + ConfigParameter.ENCODE_KEY.key() + "]");
            throw new IllegalStateException("encoding key is undefined.");
        }
        encKey = new SecretKeySpec(Hex.decodeHex(strEncKey.toCharArray()), "AES");

        final String strHmacKey = service().getConfiguration().getString(ConfigParameter.HMAC_KEY.key());
        if(strHmacKey == null) {
            getLogger().error("HMAC key is undefined. To fix it is necessary to set " +
                    "configuration parameter [" + ConfigParameter.HMAC_KEY.key() + "]");
            throw new IllegalStateException("HMAC key is undefined.");
        }
        hmacKey = new SecretKeySpec(Hex.decodeHex(strHmacKey.toCharArray()), "HmacSHA1");
    }

    @Override
    public String getTid(String serviceName) {
        return "SH1AS128CBC";
    }

    @Override
    public byte[] generateIv(String tid) {
        final byte[] iv = new byte[16];
        secureRandom.get().nextBytes(iv);
        return iv;
    }

    @Override
    public byte[] encrypt(String tid, byte[] iv, byte[] plainText) throws CryptoException {
        final Cipher cph = cipher.get();
        try {
            cph.init(Cipher.ENCRYPT_MODE, encKey, new IvParameterSpec(iv));
        } catch (InvalidKeyException e) {
            throw new CryptoException(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            throw new CryptoException(e.getMessage());
        }
        try {
            return cph.doFinal(plainText);
        } catch (IllegalBlockSizeException e) {
            throw new CryptoException(e.getMessage());
        } catch (BadPaddingException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    @Override
    public byte[] decrypt(String tid, byte[] iv, byte[] cipherText) throws CryptoException {
        final Cipher cph = cipher.get();
        try {
            cph.init(Cipher.DECRYPT_MODE, encKey, new IvParameterSpec(iv));
        } catch (InvalidKeyException e) {
            throw new CryptoException(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            throw new CryptoException(e.getMessage());
        }
        try {
            return cph.doFinal(cipherText);
        } catch (IllegalBlockSizeException e) {
            throw new CryptoException(e.getMessage());
        } catch (BadPaddingException e) {
            throw new CryptoException(e.getMessage());
        }
    }

    @Override
    public byte[] createHmac(String tid, String msg) {
        final byte[] in = StringUtils.getBytesUtf8(msg);
        return mac.get().doFinal(in);
    }

    @Override
    public boolean verifyHmac(String tid, byte[] tag, String msg) {
        final byte[] calculated = createHmac(tid, msg);
        boolean equal = true;
        for(int i = 0; i < calculated.length; i++) {
            equal &= calculated[i] == tag[i];
        }
        return equal;
    }

}
