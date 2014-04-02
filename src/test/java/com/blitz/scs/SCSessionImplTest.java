package com.blitz.scs;

import com.blitz.scs.error.SCSBrokenException;
import com.blitz.scs.error.SCSException;
import com.blitz.scs.service.spi.CryptoTransformationService;
import junit.framework.Assert;
import org.apache.commons.codec.binary.Base64;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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

        SimpleCryptoService cryptoService = new SimpleCryptoService();
        cryptoService.init("PZ84RGBeLN_S9n-sViQTnQ", encKey, hmacKey);
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

        SimpleCryptoService cryptoService = new SimpleCryptoService();
        cryptoService.init("uWArYb9mV08tboY8DSVylA", encKey, hmacKey);
        SCSession session = new SCSessionImpl(state, new Date(1396350513L * 1000), true, cryptoService);
        Assert.assertEquals(originalScs, session.asString());
    }

    @Test
    public void scsDecodingTest() throws SCSException {
        final String originalScs = "XBgdUDDGBuT8KuNlVXY6gOAkMXDeSkN6diVF5z778kI|MTM5NjM0ODcyMA|U0gxQVNDQkMxMjg|PZ84RGBeLN_S9n-sViQTnQ|BURedPx1UPuq0LTKRaPHuvqOcPM";
        final String state = "some state value";
        final byte[] encKey = "0123456789abcdef".getBytes();
        final byte[] hmacKey = "01234567890123456789".getBytes();
        final long atime = 1396348720;

        SimpleCryptoService cryptoService = new SimpleCryptoService();
        cryptoService.init("PZ84RGBeLN_S9n-sViQTnQ", encKey, hmacKey);
        SCSession session = new SCSessionImpl(false, cryptoService, originalScs);

        Assert.assertEquals(state, session.getData());
        Assert.assertEquals(new Date(atime * 1000), session.getAtime());
        Assert.assertEquals(cryptoService.getTid("someService"), session.getTid());
        org.junit.Assert.assertArrayEquals(cryptoService.generateIv(session.getTid()), session.getIv());
    }

    @Test
    public void scsCompressedDecodingTest() throws SCSException {
        final String originalScs = "OJHgHjTcJWLjDZ-ks8DE1MECshGta84lK4-lT49LfOk|MTM5NjM1MDUxMw|U0gxQVNDQkMxMjg|uWArYb9mV08tboY8DSVylA|2iOPyF4jP7RCkPYBAFHudo7XnFw";
        final String state = "some state value";
        final byte[] encKey = "0123456789abcdef".getBytes();
        final byte[] hmacKey = "01234567890123456789".getBytes();
        final long atime = 1396350513;

        SimpleCryptoService cryptoService = new SimpleCryptoService();
        cryptoService.init("uWArYb9mV08tboY8DSVylA", encKey, hmacKey);
        SCSession session = new SCSessionImpl(true, cryptoService, originalScs);

        Assert.assertEquals(originalScs, session.asString());
        Assert.assertEquals(state, session.getData());
        Assert.assertEquals(new Date(atime * 1000), session.getAtime());
        Assert.assertEquals(cryptoService.getTid("someService"), session.getTid());
        org.junit.Assert.assertArrayEquals(cryptoService.generateIv(session.getTid()), session.getIv());
    }

    @Test(expected = SCSBrokenException.class)
    public void scsBrokenMacDecodingTest() throws SCSException {
        final String originalScs = "XBgdUDDGBuT8KuNlVXY6gOAkMXDeSkN6diVF5z778kI|MTM5NjM0ODcyMA|U0gxQVNDQkMxMjg|PZ84RGBeLN_S9n-sViQTnQ|BURedPx1UPuq0LTLRaPHuvqOcPM";
        final String state = "some state value";
        final byte[] encKey = "0123456789abcdef".getBytes();
        final byte[] hmacKey = "01234567890123456789".getBytes();
        final long atime = 1396348720;

        SimpleCryptoService cryptoService = new SimpleCryptoService();
        cryptoService.init("PZ84RGBeLN_S9n-sViQTnQ", encKey, hmacKey);
        SCSession session = new SCSessionImpl(false, cryptoService, originalScs);
    }

    @Test
    public void scsSCSServiceTest() throws SCSException {
        HttpServletRequest requestMock = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(requestMock.getCookies()).andReturn(new Cookie[]{});
        EasyMock.replay(requestMock);

        final SCSService service = new SCSService();
        SCSession session = service.extractFromUpstream(requestMock);
        Assert.assertNull(session);

        //TODO write

    }

}
