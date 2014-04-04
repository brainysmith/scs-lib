package com.identityblitz.scs.glue;

import com.identityblitz.scs.service.spi.ConfigurationService;

/**
 * The basic implementation {@link com.identityblitz.scs.service.spi.ConfigurationService} which reads
 * parameters from Java properties.
 */
public class BasicConfigurationService implements ConfigurationService {

    @Override
    public Long getLong(String name) {
        return Long.getLong(name);
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        return Long.getLong(name, defaultValue);
    }

    @Override
    public String getString(String name) {
        return System.getProperty(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    @Override
    public Boolean getBoolean(String name) {
        String val = System.getProperty(name);
        return (val == null)?null:Boolean.parseBoolean(val);
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        String val = System.getProperty(name);
        return (val == null)?defaultValue:Boolean.parseBoolean(val);
    }

}
