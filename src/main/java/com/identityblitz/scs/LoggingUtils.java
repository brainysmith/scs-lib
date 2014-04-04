package com.identityblitz.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LoggingUtils {
    INSTANCE;

    private final Logger logger;

    private LoggingUtils() {
        logger = LoggerFactory.getLogger("com.identityblitz.scs");
    }

    public static Logger getLogger() {
        return INSTANCE.logger;
    }
}
