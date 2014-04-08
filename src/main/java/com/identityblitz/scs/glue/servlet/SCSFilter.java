package com.identityblitz.scs.glue.servlet;

import com.identityblitz.scs.ConfigParameter;
import com.identityblitz.scs.SCSService;
import com.identityblitz.scs.SCSession;
import com.identityblitz.scs.error.SCSException;
import com.identityblitz.scs.service.ServiceProvider;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

import static com.identityblitz.scs.LoggingUtils.getLogger;

/**
 * This HTTP servlet filter allows to add possibility to store session state by using Secure Cookie Session.
 * The filter must be added in the chain before the chain's elements that can try to get access to session state.
 * To get current session state it is necessary to use the function
 * {@link com.identityblitz.scs.SCSService#getSCS(Object)}
 * and to change the current session state - the function
 * {@link com.identityblitz.scs.SCSService#changeSCS(Object, String)}.
 * The filter has only one boolean option to configure <b>com.blitz.scs.useCompression</b>. This option turns off/on
 * using of compression session state. Default value is not to use compression.
 */
public class SCSFilter implements Filter {
    private SCSService scsService = new SCSService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        final boolean useCompression =
                ServiceProvider.INSTANCE.getConfiguration().getBoolean(ConfigParameter.USE_COMPRESSION.key(), false);
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
        chain.doFilter(request, new ScsHttpServletResponse((HttpServletResponse)response, httpRequest, scsService));
        try {
            if(response.isCommitted()) {
                getLogger().warn("Response is already committed so SCS cookie will not be set and all session state changes " +
                        "made during processing the current request will be lost.");
            }
            else {
                final SCSession session = scsService.putIntoDownstream((HttpServletResponse)response, httpRequest);
                getLogger().debug("Session put into downstream: {}.", session);
            }
        } catch (SCSException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {}
}

class ScsHttpServletResponse extends HttpServletResponseWrapper {
    private final HttpServletRequest request;
    private final SCSService scsService;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response
     * @throws IllegalArgumentException if the response is null
     */
    public ScsHttpServletResponse(HttpServletResponse response, HttpServletRequest request, SCSService scsService) {
        super(response);
        this.request = request;
        this.scsService = scsService;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if(isCommitted()) {
            getLogger().warn("Response is already committed so SCS cookie will not be set and all session state changes " +
                    "made during processing the current request will be lost.");
        }
        else {
            final SCSession session;
            try {
                session = scsService.putIntoDownstream(this, request);
                getLogger().debug("Session put into downstream: {}.", session);
            } catch (SCSException e) {
                getLogger().error("A error occurred while encoding SCS: {}.", e.getMessage());
            }
        }
        super.sendRedirect(location);
    }
}
