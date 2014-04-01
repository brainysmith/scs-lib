package com.blitz.scs.service;

/**
 * The generic exception for errors relating to cryptographic operations
 */
public class CryptoException extends Exception {
    public CryptoException(String message) {
        super(message);
    }
}
