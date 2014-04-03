package com.blitz.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LoggingUtils {
    INSTANCE;

    private final Logger logger;

    private LoggingUtils() {
        logger = LoggerFactory.getLogger("com.blitz.scs");
    }

    public static Logger getLogger() {
        return INSTANCE.logger;
    }
}
