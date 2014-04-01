package com.blitz.scs.service;

/**
 * Service that provides basic cryptographic operations used in processing of SCS.
 */
public interface CryptoTransformationService {

    /**
     * Returns the current TID (transformation set ID) for the service specified by its name.
     * @param serviceName - service name.
     * @return - current TID.
     */
    public String getTid(final String serviceName);

    /**
     * Generates arbitrary length IV.
     * @param length - IV length.
     * @return - generated IV.
     */
    public byte[] generateIv(int length);

    /**
     * Encrypts plain data with algorithm corresponding to the specified transformation identifier.
     * @param tid - cryptographic transformation set.
     * @param plain - plain text to encrypt.
     * @return - encoded data.
     */
    public byte[] encrypt(final String tid, final byte[] iv, final byte[] plain) throws CryptoException;

    /**
     * Decrypts the specified upper hex cipher text with algorithm corresponding to
     * the specified transformation identifier.
     * @param tid - cryptographic transformation set.
     * @param cipher - cipher text to decrypt.
     * @return - decrypted data.
     */
    public byte[] decrypt(final String tid, final byte[] iv, final byte[] cipher) throws CryptoException;

    /**
     * Calculates HMAC of the specified massage with algorithm corresponding to the specified transformation identifier.
     * Before calculation the message is converted from string into the byte array, using UTF-8 encoding.
     * @param tid - cryptographic transformation set.
     * @param msg - message to calculate HMAC of.
     * @return - calculated HMAC as byte array.
     */
    public byte[] createHmac(final String tid, final String msg);

    /**
     * Verifies HMAC against message with algorithm corresponding to the specified transformation identifier.
     * Before calculation the message is converted from string into the byte array, using UTF-8 encoding.
     * @param tid - cryptographic transformation set.
     * @param tag - HMAC tag to verify.
     * @param msg - a message to verify against.
     * @return - false - if HMAC wrong;
     *           true - if HMAC correct.
     */
    public boolean verifyHmac(final String tid, final byte[] tag, final String msg);

}
