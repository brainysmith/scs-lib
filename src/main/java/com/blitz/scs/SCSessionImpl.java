package com.blitz.scs;

import com.blitz.scs.error.SCSException;
import com.blitz.scs.service.CryptoTransformationService;
import org.apache.commons.codec.binary.Base64;
import java.util.Date;
import static com.blitz.scs.DeflateUtils.deflate;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

class SCSessionImpl implements SCSession {
    private static final char FIELD_SEPARATOR = '|';
    private static final String CRYPTO_TID = "SH1ASCBC128";
    private static final int ivLength = 16;

    private final String data;
    private final byte[] encData;
    private final Date atime;
    private final String tid;
    private final byte[] iv;
    private final byte[] authTag;

    SCSessionImpl(final String data, final boolean compress, final CryptoTransformationService crypto)
            throws SCSException {
        this(data, new Date(), compress, crypto);
    }

    SCSessionImpl(final String data, final Date atime, final boolean compress, final CryptoTransformationService crypto)
            throws SCSException {
        this.data = data;
        this.tid = CRYPTO_TID;
        this.iv = crypto.generateIv(ivLength);
        this.encData = crypto.encrypt(this.tid, this.iv, compress?deflate(this.data):getBytesUtf8(this.data));
        this.atime = atime;
        this.authTag = crypto.createHmac(this.tid, box(
                Base64.encodeBase64URLSafeString(this.encData),
                Base64.encodeBase64URLSafeString(getBytesUtf8(Long.toString(this.atime.getTime() / 1000))),
                Base64.encodeBase64URLSafeString(getBytesUtf8(this.tid)),
                Base64.encodeBase64URLSafeString(this.iv)));
    }

    @Override
    public String asString() throws SCSException {
        return box(Base64.encodeBase64URLSafeString(this.encData),
                Base64.encodeBase64URLSafeString(getBytesUtf8(Long.toString(this.atime.getTime() / 1000))),
                Base64.encodeBase64URLSafeString(getBytesUtf8(this.tid)),
                Base64.encodeBase64URLSafeString(this.iv),
                Base64.encodeBase64URLSafeString(this.authTag));
    }

    @Override public String getData() {return data;}
    @Override public Date getAtime() {return atime;}
    @Override public String getTid() {return tid;}
    @Override public byte[] getIv() {return iv;}
    @Override public byte[] getAuthTag() {return authTag;}

    private static String box(String... args) {
        StringBuilder builder = new StringBuilder(128);
        for(String arg : args)
            builder.append(arg).append(FIELD_SEPARATOR);
        if(args.length > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

}
