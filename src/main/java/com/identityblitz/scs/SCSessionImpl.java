package com.identityblitz.scs;

import com.identityblitz.scs.error.SCSBrokenException;
import com.identityblitz.scs.error.SCSException;
import com.identityblitz.scs.error.SCSExpiredException;
import com.identityblitz.scs.service.ServiceProvider;
import com.identityblitz.scs.service.spi.CryptoException;
import com.identityblitz.scs.service.spi.CryptoTransformationService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import java.util.Date;
import static com.identityblitz.scs.DeflateUtils.deflate;
import static com.identityblitz.scs.DeflateUtils.inflate;
import static com.identityblitz.scs.LoggingUtils.getLogger;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;

final class SCSessionImpl implements SCSession {
    private static final char FIELD_SEPARATOR = '|';
    private static final String SERVICE_NAME = "com.identityblitz.scs.Service";
    private static final long SESSION_MAX_AGE_IN_SEC =
            ServiceProvider.INSTANCE.getConfiguration().getLong(ConfigParameter.SESSION_MAX_AGE.key(), 3600L);

    private final String data;
    private final byte[] encData;
    private final Date atime;
    private final String tid;
    private final byte[] iv;
    private final byte[] authTag;

    SCSessionImpl(final String data, final boolean compressed, final CryptoTransformationService crypto)
            throws SCSException {
        this(data, new Date(), compressed, crypto);
    }

    SCSessionImpl(final String data, final Date atime, final boolean compressed, final CryptoTransformationService crypto)
            throws SCSException {
        this.data = data;
        this.tid = crypto.getTid(SERVICE_NAME);
        this.iv = crypto.generateIv(this.tid);
        try {
            this.encData = crypto.encrypt(this.tid, this.iv, compressed?deflate(this.data):getBytesUtf8(this.data));
        } catch (CryptoException e) {
            throw new SCSException(e.getMessage());
        }
        this.atime = atime;
        this.authTag = crypto.createHmac(this.tid, box(
                Base64.encodeBase64URLSafeString(this.encData),
                Base64.encodeBase64URLSafeString(getBytesUtf8(Long.toString(this.atime.getTime() / 1000))),
                Base64.encodeBase64URLSafeString(getBytesUtf8(this.tid)),
                Base64.encodeBase64URLSafeString(this.iv)));
        getLogger().debug("Created SCS with data = {}, atime = {}, tid = {}, iv = {}, authTag = {}.", new Object[]{
                this.data, this.atime, this.tid, Base64.encodeBase64String(this.iv), Base64.encodeBase64String(this.authTag)});
    }

    SCSessionImpl(final boolean compressed, final CryptoTransformationService crypto, final String scs)
            throws SCSException {
        final String[] parts = scs.split("\\|");
        if(parts.length != 5) {
            getLogger().warn("SCS {} is broken.", scs);
            throw new SCSBrokenException("SCS haven't go all parts");
        }

        this.tid = StringUtils.newStringUtf8(Base64.decodeBase64(parts[2]));
        this.authTag = Base64.decodeBase64(parts[4]);
        if(!crypto.verifyHmac(tid, authTag, box(parts[0], parts[1], parts[2], parts[3]))) {
            getLogger().warn("SCS {} has a wrong mac.", scs);
            throw new SCSBrokenException("mac is wrong");
        }

        final long atimeInSec = Long.valueOf(StringUtils.newStringUtf8(Base64.decodeBase64(parts[1])));
        if(atimeInSec + SESSION_MAX_AGE_IN_SEC < (new Date().getTime() / 1000)) {
            getLogger().info("SCS {} is expired", scs);
            throw new SCSExpiredException(new Date(atimeInSec * 1000), new Date());
        }
        this.atime = new Date(atimeInSec * 1000);
        this.iv = Base64.decodeBase64(parts[3]);
        this.encData = Base64.decodeBase64(parts[0]);
        try {
            this.data = StringUtils.newStringUtf8((compressed)?
                    inflate(crypto.decrypt(this.tid, this.iv, this.encData)):
                    crypto.decrypt(this.tid, this.iv, this.encData));
        } catch (CryptoException e) {
            throw new SCSException(e.getMessage());
        }
        getLogger().debug("Parsed SCS with data = {}, atime = {}, tid = {}, iv = {}, authTag = {}.", new Object[]{
                this.data, this.atime, this.tid, Base64.encodeBase64String(this.iv), Base64.encodeBase64String(this.authTag)});
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

    @Override
    public String toString() {
        return "SCSessionImpl{" +
                "data='" + data + '\'' +
                ", atime=" + atime +
                '}';
    }

}
