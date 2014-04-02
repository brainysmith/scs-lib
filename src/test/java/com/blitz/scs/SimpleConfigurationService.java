package com.blitz.scs;

import com.blitz.scs.service.spi.ConfigurationService;
import java.util.HashMap;
import java.util.Map;

public class SimpleConfigurationService implements ConfigurationService {

    private final Map<String, Object> config = new HashMap<String, Object>() {
        {
            put("com.blitz.scs.sessionMaxAgeInSec", 7 * 365 * 86400L);
            put("com.blitz.scs.cookieDomain", "blitz.com");
        }
    };

    @Override
    public Long getLong(String name) {
        return (Long)config.get(name);
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        Long value = (Long)config.get(name);
        return (value != null)?value:defaultValue;
    }

    @Override
    public String getString(String name) {
        return (String)config.get(name);
    }

    @Override
    public String getString(String name, String defaultValue) {
        String value = (String) config.get(name);
        return (value != null) ? value : defaultValue;
    }

    @Override
    public Boolean getBoolean(String name) {
        return (Boolean)config.get(name);
    }

    @Override
    public Boolean getBoolean(String name, Boolean defaultValue) {
        Boolean value = (Boolean)config.get(name);
        return (value != null)?value:defaultValue;
    }

}
