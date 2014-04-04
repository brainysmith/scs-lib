package com.identityblitz.scs.service.spi;

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
    public Long getLong(final String name, final Long defaultValue);

    /**
     * Returns a configuration value corresponding to the specified name.
     * @param name - name of configuration parameter.
     * @return - parameter value.
     */
    public String getString(final String name);

    /**
     * Returns a configuration value corresponding to the specified name. If the value is not specified in
     * configuration the default value is returned.
     * @param name - parameter name.
     * @param defaultValue - default value.
     * @return - parameter value.
     */
    public String getString(final String name, final String defaultValue);

    /**
     * Returns a configuration value corresponding to the specified name.
     * @param name - name of configuration parameter.
     * @return - parameter value.
     */
    public Boolean getBoolean(final String name);

    /**
     * Returns a configuration value corresponding to the specified name. If the value is not specified in
     * configuration the default value is returned.
     * @param name - parameter name.
     * @param defaultValue - default value.
     * @return - parameter value.
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue);

}
