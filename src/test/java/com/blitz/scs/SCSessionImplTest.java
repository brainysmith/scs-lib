package com.blitz.scs;

import com.blitz.scs.error.SCSException;
import com.blitz.scs.service.CryptoTransformationService;
import junit.framework.Assert;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Date;

public class SCSessionImplTest {

    @BeforeClass
    public static void setUp() throws Throwable {}

    @Test
    public void scsEncodingTest() throws SCSException {
        final String originalScs = "XBgdUDDGBuT8KuNlVXY6gOAkMXDeSkN6diVF5z778kI|MTM5NjM0ODcyMA|U0gxQVNDQkMxMjg|PZ84RGBeLN_S9n-sViQTnQ|BURedPx1UPuq0LTKRaPHuvqOcPM";
        final String state = "some state value";
        final byte[] encKey = "0123456789abcdef".getBytes();
        final byte[] hmacKey = "01234567890123456789".getBytes();
        final byte[] iv = Base64.decodeBase64("PZ84RGBeLN_S9n-sViQTnQ");

        CryptoTransformationService cryptoService = new SimpleCryptoService("PZ84RGBeLN_S9n-sViQTnQ", encKey, hmacKey);
        SCSession session = new SCSessionImpl(state, new Date(1396348720L * 1000), false, cryptoService);
        Assert.assertEquals(originalScs, session.asString());
    }

    @Test
    public void scsCompressedEncodingTest() throws SCSException {
        final String originalScs = "OJHgHjTcJWLjDZ-ks8DE1MECshGta84lK4-lT49LfOk|MTM5NjM1MDUxMw|U0gxQVNDQkMxMjg|uWArYb9mV08tboY8DSVylA|2iOPyF4jP7RCkPYBAFHudo7XnFw";
        final String state = "some state value";
        final byte[] encKey = "0123456789abcdef".getBytes();
        final byte[] hmacKey = "01234567890123456789".getBytes();
        final byte[] iv = Base64.decodeBase64("uWArYb9mV08tboY8DSVylA");

        CryptoTransformationService cryptoService = new SimpleCryptoService("uWArYb9mV08tboY8DSVylA", encKey, hmacKey);
        SCSession session = new SCSessionImpl(state, new Date(1396350513L * 1000), true, cryptoService);
        Assert.assertEquals(originalScs, session.asString());
    }

    @Test
    public void scsDecodingTest() throws SCSException {
        final String originalScs = "XBgdUDDGBuT8KuNlVXY6gOAkMXDeSkN6diVF5z778kI|MTM5NjM0ODcyMA|U0gxQVNDQkMxMjg|PZ84RGBeLN_S9n-sViQTnQ|BURedPx1UPuq0LTKRaPHuvqOcPM";
        final String state = "some state value";
        final byte[] encKey = "0123456789abcdef".getBytes();
        final byte[] hmacKey = "01234567890123456789".getBytes();

        CryptoTransformationService cryptoService = new SimpleCryptoService("PZ84RGBeLN_S9n-sViQTnQ", encKey, hmacKey);
        SCSession session = new SCSessionImpl(false, cryptoService, originalScs);
    }

}
