package com.blitz.scs;

import com.blitz.scs.service.spi.ConfigurationService;
import java.util.HashMap;
import java.util.Map;

public class SimpleConfigurationService implements ConfigurationService {

    private final Map<String, Long> config = new HashMap<String, Long>() {
        {
            put("com.blitz.scs.sessionMaxAgeInSec", 7 * 365 * 86400L);
        }
    };

    @Override
    public Long getLong(String name) {
        return config.get(name);
    }

    @Override
    public Long getLong(String name, Long defaultValue) {
        Long value = config.get(name);
        return (value != null)?value:defaultValue;
    }

}
