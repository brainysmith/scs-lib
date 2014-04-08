package com.identityblitz.scs;

public enum ConfigParameter {
    SCS_COOKIE_NAME("com.identityblitz.scs.cookieName"),
    DOMAIN ("com.identityblitz.scs.cookieDomain"),
    IS_SECURE("com.identityblitz.scs.cookieIsSecure"),
    PATH("com.identityblitz.scs.cookiePath"),
    PLATFORM("com.identityblitz.scs.Platform"),
    USE_COMPRESSION("com.identityblitz.scs.useCompression"),
    SESSION_MAX_AGE("com.identityblitz.scs.sessionMaxAgeInSec");

    private String key;

    private ConfigParameter(final String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
