package com.identityblitz.scs;

import com.identityblitz.scs.error.SCSException;

import java.util.Date;

/**
 * This class represents Secure Cookie Session. It conforms the structure described
 * in RFC 6896 (https://tools.ietf.org/html/rfc6896).
 */
public interface SCSession {

    /**
     * Returns block of session data carrying by this SCS.
     * @return - session payload data.
     */
    public String getData();

    /**
     * Return an absolute timestamp relating to the last read or write operation performed on session data.
     * @return - last timestamp of touch of session data.
     */
    public Date getAtime();

    /**
     * Returns an identifier that consists of ASCII characters and uniquely determines the
     * transformation set (keys and algorithms) used to generate this SCS.
     * @return - identifier of transformation set.
     */
    public String getTid();

    /**
     * Returns an initialization vector used for encryption.
     * @return - IV
     */
    public byte[] getIv();

    /**
     * Returns an authentication tag that ensures integrity and authenticity of SCS.
     * @return - authentication tag
     */
    public byte[] getAuthTag();

    /**
     * Returns a string representation of this SCS.
     * @return - string representation of this SCS.
     */
    public String asString() throws SCSException;

}
