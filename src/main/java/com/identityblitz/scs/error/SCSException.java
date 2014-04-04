package com.identityblitz.scs.error;

/**
 * Generic exception for error occurred while SCS processing.
 */
public class SCSException extends Exception {

    public SCSException(String message) {
        super(message);
    }
}
