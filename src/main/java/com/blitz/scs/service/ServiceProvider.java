package com.blitz.scs.service;

import com.blitz.scs.service.spi.CryptoTransformationService;

/**
 * The service provider.
 */
public enum ServiceProvider {
    INSTANCE;

    private static final CryptoTransformationService cryptoService;

    static {
        cryptoService = null;
    }

    public CryptoTransformationService getCryptoService() {
        return cryptoService;
    }
}
