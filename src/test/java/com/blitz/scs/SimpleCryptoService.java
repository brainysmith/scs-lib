package com.blitz.scs;

import com.blitz.scs.service.CryptoException;
import com.blitz.scs.service.CryptoTransformationService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.util.Arrays;

public class SimpleCryptoService implements CryptoTransformationService {
    private final byte[] iv;
    private final BufferedBlockCipher cipher;
    private final KeyParameter encKeyParam;
    private final Mac mac;


    public SimpleCryptoService(final String base64Iv, final byte[] encKey, final byte[] hmacKey) {
        iv = Base64.decodeBase64(base64Iv);
        cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        encKeyParam = new KeyParameter(encKey);
        mac = new HMac(new SHA1Digest());
        mac.init(new KeyParameter(hmacKey));
    }

    @Override
    public String getTid(String serviceName) {
        return "SH1ASCBC128";
    }

    @Override
    public byte[] generateIv(int length) {
        return iv;
    }

    @Override
    public byte[] encrypt(String tid, byte[] iv, byte[] plain) throws CryptoException {
        ParametersWithIV params = new ParametersWithIV(encKeyParam, iv);
        cipher.init(true, params);
        final byte[] processed = new byte[cipher.getOutputSize(plain.length)];
        int outputLength = cipher.processBytes(plain, 0, plain.length, processed, 0);
        try {
            outputLength += cipher.doFinal(processed, outputLength);
        } catch (InvalidCipherTextException e) {
            throw new CryptoException(e.getMessage());
        }
        return processed;
    }

    @Override
    public byte[] decrypt(String tid, byte[] iv, byte[] cipherText) throws CryptoException {
        ParametersWithIV params = new ParametersWithIV(encKeyParam, iv);
        cipher.init(false, params);
        final byte[] plainText = new byte[cipher.getOutputSize(cipherText.length)];
        int outputLength = cipher.processBytes(cipherText, 0, cipherText.length, plainText, 0);
        try {
            outputLength += cipher.doFinal(plainText, outputLength);
        } catch (InvalidCipherTextException e) {
            throw new CryptoException(e.getMessage());
        }
        final byte[] result = new byte[outputLength];
        System.arraycopy(plainText, 0, result, 0, outputLength);
        return result;
    }

    @Override
    public byte[] createHmac(String tid, String msg) {
        final byte[] result = new byte[mac.getMacSize()];
        final byte[] in = StringUtils.getBytesUtf8(msg);
        mac.update(in, 0, in.length);
        mac.doFinal(result, 0);
        return result;
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
