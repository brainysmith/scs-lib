package com.identityblitz.scs.service;

import com.identityblitz.scs.service.spi.ConfigurationService;
import com.identityblitz.scs.service.spi.CryptoTransformationService;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * The service provider allows to obtain the following services:
 *  - cryptographic transformation service;
 *  - configuration service.
 */
public enum ServiceProvider {
    INSTANCE;

    private static final CryptoTransformationService cryptoService;
    private static final ConfigurationService configService;

    static {
        final Iterator<ConfigurationService> cItr =
                ServiceLoader.load(ConfigurationService.class).iterator();
        if(!cItr.hasNext())
            throw new RuntimeException("configuration service is undefined.");
        configService = cItr.next();

        final Iterator<CryptoTransformationService> ctsItr =
                ServiceLoader.load(CryptoTransformationService.class).iterator();
        if(!ctsItr.hasNext())
            throw new RuntimeException("cryptographic transformation service is undefined.");
        cryptoService = ctsItr.next();
    }

    public static ServiceProvider service() {
        return INSTANCE;
    }

    public CryptoTransformationService getCryptoService() {
        return cryptoService;
    }

    public ConfigurationService getConfiguration() {
        return configService;
    }
}
