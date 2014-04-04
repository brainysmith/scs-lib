package com.identityblitz.scs.error;

/**
 * This SCS exception points that content of SCS is broken. For instance:
 *  - HMAC is wrong;
 *  - cipher text is broken.
 */
public class SCSBrokenException extends SCSException {
    public SCSBrokenException(String message) {
        super(message);
    }
}
