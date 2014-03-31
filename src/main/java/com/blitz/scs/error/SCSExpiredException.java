package com.blitz.scs.error;

import java.util.Date;

/**
 * The exception points that the SCS is expired.
 */
public class SCSExpiredException extends SCSException {
    public SCSExpiredException(final Date atime, final Date now) {
        super("The SCS is expired. ATIME is " + atime + " and NOW is " + now + ".");
    }
}
