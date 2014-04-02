package com.blitz.scs;

import com.blitz.scs.error.SCSException;
import com.blitz.scs.service.ServiceProvider;
import com.blitz.scs.service.spi.CryptoTransformationService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The service provides operations to work with SCS. The basic operations to encode and decode SCS.
 * Some handy operations to handle SCS with HTTP requests.
 */
public final class SCSService {
    private static final String SCS_COOKIE_NAME = ServiceProvider.INSTANCE.getConfiguration()
            .getString("com.blitz.scs.cookieName", "SCS");
    private static final String SCS_ATTRIBUTE_NAME = "com.blitz.scs.requestAttribute";
    private static final String DOMAIN = ServiceProvider.INSTANCE.getConfiguration()
            .getString("com.blitz.scs.cookieDomain");
    private static final boolean IS_SECURE = ServiceProvider.INSTANCE.getConfiguration()
            .getBoolean("com.blitz.scs.cookieIsSecure", false);
    private static final String PATH = ServiceProvider.INSTANCE.getConfiguration()
            .getString("com.blitz.scs.cookiePath", "/");

    private boolean useCompression;
    private CryptoTransformationService cryptoService;

    public SCSService() {
        this.useCompression = false;
        cryptoService = ServiceProvider.INSTANCE.getCryptoService();
    }

    public void init(final boolean useCompression) {
        this.useCompression = useCompression;
    }

    /**
     * Encodes SCS containing the specified session information and returns it.
     * @param session - session information.
     * @return - SCS.
     * @throws SCSException - if any errors occurred while encoding.
     */
    public SCSession encode(final String session) throws SCSException {
        return new SCSessionImpl(session, useCompression, cryptoService);
    }

    /**
     * Decodes the specified string representation of SCS, turns it into {@link com.blitz.scs.SCSession} object
     * and returns it. While decoding it does all necessary checks including the expiration check.
     * @param scs - string representation of SCS.
     * @return - SCS
     * @throws com.blitz.scs.error.SCSExpiredException - if SCS is expired.
     * @throws com.blitz.scs.error.SCSBrokenException - if SCS is broken.
     * @throws SCSException - if any other error which doesn't fall into previous two ones.
     */
    public SCSession decode(final String scs) throws SCSException {
        return new SCSessionImpl(useCompression, cryptoService, scs);
    }

    /**
     * Extracts the SCS from the passed HTTP request. If cookie with name specified by configuration parameter
     * <b>com.blitz.scs.cookieName<b/> (default value of the parameter is SCS) is not found the function returns null.
     * Otherwise the cookie's value is decoded and checked. If decoding and checking finish successfully
     * the {@link com.blitz.scs.SCSession} object is put into the HTTP request as an attribute and also returned.
     * @param request - HTTP request.
     * @return - SCS.
     * @throws com.blitz.scs.error.SCSExpiredException - if SCS is expired.
     * @throws com.blitz.scs.error.SCSBrokenException - if SCS is broken.
     * @throws SCSException - if any other error which doesn't fall into previous two ones.
     */
    public SCSession extractFromUpstream(final HttpServletRequest request) throws SCSException {
        final Cookie scsCookie = findCookie(request, SCS_COOKIE_NAME);
        if(scsCookie == null)
            return null;

        final SCSession session = decode(scsCookie.getValue());
        request.setAttribute(SCS_ATTRIBUTE_NAME, session.getData());
        return session;
    }

    /**
     * Returns the current session state attached to the passed request. If no session state attached to the request returns null.
     * Before a call to this function it necessary to call function
     * {@link com.blitz.scs.SCSService#extractFromUpstream(javax.servlet.http.HttpServletRequest)} first.
     * Otherwise this function returns null though the request actually has a valid SCS cookie.
     * @param request - HTTP request.
     * @return - current session state.
     */
    public String getCurrentSCS(final HttpServletRequest request) {
        return (String)request.getAttribute(SCS_ATTRIBUTE_NAME);
    }

    /**
     * Sets a passed session sate as the current session state.
     * @param request - HTTP request.
     * @param newSessionState - new session state.
     */
    public void changeCurrentSCS(final HttpServletRequest request, final String newSessionState) {
        request.setAttribute(SCS_ATTRIBUTE_NAME, newSessionState);
    }

    /**
     * Encodes the current session state into a SCS, puts the obtained SCS into the response as a SCS cookie
     * and also returns it. If there is no current session state the function returns null.
     * @param response - HTTP response.
     * @param request - HTTP request.
     * @return - SCS.
     * @throws SCSException - if an error occurred while processing the SCS.
     */
    public SCSession putIntoDownstream(final HttpServletResponse response, final HttpServletRequest request) throws SCSException {
        String currentState = getCurrentSCS(request);
        if(currentState == null) {
            final SCSession prevSession = extractFromUpstream(request);
            if(prevSession != null) {
                currentState = prevSession.getData();
            }
        }
        if(currentState != null) {
            final SCSession session  = encode(currentState);
            response.addCookie(createSCSCookie(session));
            return session;
        }
        else {
            return null;
        }
    }

    private static Cookie findCookie(final HttpServletRequest request, final String cookieName) {
        for(Cookie cookie : request.getCookies()) {
            if(cookieName.equals(cookie.getName()))
                return cookie;
        }
        return null;
    }

    private static Cookie createSCSCookie(final SCSession scs) throws SCSException {
        final Cookie scsCookie = new Cookie(SCS_COOKIE_NAME, scs.asString());
        scsCookie.setDomain(DOMAIN);
        scsCookie.setSecure(IS_SECURE);
        scsCookie.setHttpOnly(true);
        scsCookie.setPath(PATH);
        return scsCookie;
    }

}
