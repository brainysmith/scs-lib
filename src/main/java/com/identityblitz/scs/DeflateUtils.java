package com.identityblitz.scs;

import com.identityblitz.scs.error.SCSException;
import org.apache.commons.codec.binary.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * The collection methods to deflate and inflate.
 */
public class DeflateUtils {

    private DeflateUtils() {
        throw new UnsupportedOperationException();
    }

    public static byte[] deflate(final String data) throws SCSException {
        final byte[] toDeflate = StringUtils.getBytesUtf8(data);
        final Deflater deflater = new Deflater();
        final byte[] tmp = new byte[4096];

        deflater.setInput(toDeflate);
        deflater.finish();
        final int outSize = deflater.deflate(tmp);
        if(!deflater.finished()) {
            throw new SCSException("Can not deflate session data. Data is too large.");
        }
        deflater.end();

        final byte[] out = new byte[outSize];
        System.arraycopy(tmp, 0, out, 0, outSize);
        return out;
    }

    public static byte[] inflate(final byte[] compressed) throws SCSException {
        final Inflater inflater = new Inflater();
        final byte[] tmp = new byte[4096];

        inflater.setInput(compressed);
        final int outSize;
        try {
            outSize = inflater.inflate(tmp);
            if(!inflater.finished()) {
                throw new SCSException("Can not inflate session data. Data is too large.");
            }
        } catch (DataFormatException e) {
            throw new SCSException(e.getMessage());
        }
        inflater.end();
        final byte[] result = new byte[outSize];
        System.arraycopy(tmp, 0, result, 0, outSize);
        return result;
    }

    private static String bytes2String(final byte[] bytes, final int ofs, final int size) throws SCSException {
        try {
            return new String(bytes, ofs, size, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

}
