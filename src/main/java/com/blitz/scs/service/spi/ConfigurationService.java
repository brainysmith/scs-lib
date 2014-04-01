package com.blitz.scs.service.spi;

/**
 * The service provides access to the configuration.
 */
public interface ConfigurationService {

    /**
     * Returns a configuration value corresponding to the specified name.
     * @param name - name of configuration parameter.
     * @return - parameter value.
     */
    public Long getLong(final String name);

    /**
     * Returns a configuration value corresponding to the specified name. If the value is not specified in
     * configuration the default value is returned.
     * @param name - parameter name.
     * @param defaultValue - default value.
     * @return - parameter value.
     */
    public Long getLong(final String name, Long defaultValue);

}
