package com.identityblitz.scs.glue;

import com.identityblitz.scs.SCSService;
import com.identityblitz.scs.SCSession;
import com.identityblitz.scs.error.SCSException;
import com.identityblitz.scs.service.ServiceProvider;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.identityblitz.scs.LoggingUtils.getLogger;

/**
 * This HTTP servlet filter allows to add possibility to store session state by using Secure Cookie Session.
 * The filter must be added in the chain before the chain's elements that can try to get access to session state.
 * To get current session state it is necessary to use the function
 * {@link com.identityblitz.scs.SCSService#getCurrentSCS(javax.servlet.http.HttpServletRequest)}
 * and to change the current session state - the function
 * {@link com.identityblitz.scs.SCSService#changeCurrentSCS(javax.servlet.http.HttpServletRequest, String)}.
 * The filter has only one boolean option to configure <b>com.blitz.scs.useCompression</b>. This option turns off/on
 * using of compression session state. Default value is not to use compression.
 */
public class SCSFilter implements Filter {
    private SCSService scsService = new SCSService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final boolean useCompression =
                ServiceProvider.INSTANCE.getConfiguration().getBoolean("com.identityblitz.scs.useCompression", false);
        scsService.init(useCompression);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        try {
            final SCSession session = scsService.extractFromUpstream(httpRequest);
            getLogger().debug("Session extracted from upstream: {}.", session);
        } catch (SCSException e) {
            throw new ServletException(e);
        }
        chain.doFilter(request, response);
        try {
            final SCSession session = scsService.putIntoDownstream((HttpServletResponse)response, httpRequest);
            getLogger().debug("Session put into downstream: {}.", session);
        } catch (SCSException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {}
}
