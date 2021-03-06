package com.identityblitz.scs;

import com.identityblitz.scs.error.SCSException;
import com.identityblitz.scs.glue.netty.http.SCSFullHttpRequest;
import com.identityblitz.scs.service.ServiceProvider;
import com.identityblitz.scs.service.spi.CryptoTransformationService;
import play.mvc.Http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.identityblitz.scs.LoggingUtils.getLogger;

/**
 * The service provides operations to work with SCS. The basic operations to encode and decode SCS.
 * Some handy operations to handle SCS with HTTP requests.
 * The configuration parameters the service has is listed in the table below.
 * <table>
 *     <col width="25%"/>
 *     <col width="50%"/>
 *     <col width="25%"/>
 *     <thead>
 *         <tr><th>Name</th><th>Description</th><th>Default value</th></tr>
 *     </thead>
 *     <tbody>
 *         <tr><td>com.blitz.scs.cookieName</td><td>SCS cookie name.</td><td>SCS</td></tr>
 *         <tr><td>com.blitz.scs.cookieDomain</td><td>SCS cookie domain.</td><td></td></tr>
 *         <tr><td>com.blitz.scs.cookiePath</td><td>SCS cookie path.</td><td>/</td></tr>
 *         <tr><td>com.blitz.scs.cookieIsSecure</td><td>To transfer a SCS cookie only over SSL.</td><td>false</td></tr>
 *     </tbody>
 * </table>
 */
public final class SCSService {
    private static final String SCS_COOKIE_NAME = ServiceProvider.INSTANCE.getConfiguration()
            .getString(ConfigParameter.SCS_COOKIE_NAME.key(), "SCS");
    private static final String DOMAIN = ServiceProvider.INSTANCE.getConfiguration()
            .getString(ConfigParameter.DOMAIN.key());
    private static final boolean IS_SECURE = ServiceProvider.INSTANCE.getConfiguration()
            .getBoolean(ConfigParameter.IS_SECURE.key(), false);
    private static final String PATH = ServiceProvider.INSTANCE.getConfiguration()
            .getString(ConfigParameter.PATH.key(), "/");

    /**
     * This configuration parameter specifies the platform the SCS library is built into. The available values:
     *  - SERVLET;
     *  - NETTY-HTTP;
     *  - PLAY.
     */
    private static final Platform PLATFORM = Platform.safeValueOf(ServiceProvider.INSTANCE.getConfiguration()
            .getString(ConfigParameter.PLATFORM.key()));

    private static final String SCS_ATTRIBUTE_NAME = "com.identityblitz.scs.requestAttribute";
    private static final Set<Platform> available = checkAvailable();

    private boolean useCompression;
    private Long sSessionMaxAge;
    private CryptoTransformationService cryptoService;

    public SCSService() {
        this.useCompression = false;
        getLogger().debug("SCS cookie compression is set to false.");
        cryptoService = ServiceProvider.INSTANCE.getCryptoService();
    }

    public void init(final boolean useCompression, final Long sSessionMaxAge) {
        this.useCompression = useCompression;
        getLogger().debug("SCS cookie compression is set to {}.", this.useCompression);
        this.sSessionMaxAge = sSessionMaxAge;
        getLogger().debug("SCS session max age is set to {}.", this.useCompression);
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
     * Decodes the specified string representation of SCS, turns it into {@link com.identityblitz.scs.SCSession} object
     * and returns it. While decoding it does all necessary checks including the expiration check.
     * @param scs - string representation of SCS.
     * @return - SCS
     * @throws com.identityblitz.scs.error.SCSExpiredException - if SCS is expired.
     * @throws com.identityblitz.scs.error.SCSBrokenException - if SCS is broken.
     * @throws SCSException - if any other error which doesn't fall into previous two ones.
     */
    public SCSession decode(final String scs) throws SCSException {
        return new SCSessionImpl(useCompression, cryptoService, scs, sSessionMaxAge);
    }

    /**
     * Extracts the SCS from the passed HTTP request. If cookie with name specified by configuration parameter
     * <b>com.blitz.scs.cookieName<b/> (default value of the parameter is SCS) is not found the function returns null.
     * Otherwise the cookie's value is decoded and checked. If decoding and checking finish successfully
     * the {@link com.identityblitz.scs.SCSession} object is put into the HTTP request as an attribute and also returned.
     * @param request - HTTP request.
     * @return - SCS.
     * @throws com.identityblitz.scs.error.SCSExpiredException - if SCS is expired.
     * @throws com.identityblitz.scs.error.SCSBrokenException - if SCS is broken.
     * @throws SCSException - if any other error which doesn't fall into previous two ones.
     */
    public SCSession extractFromUpstream(final HttpServletRequest request) throws SCSException {
        final Cookie scsCookie = findCookie(request, SCS_COOKIE_NAME);
        if(scsCookie == null) {
            getLogger().debug("SCS cookie is absent in the request.");
            return null;
        }

        final SCSession session = decode(scsCookie.getValue());
        request.setAttribute(SCS_ATTRIBUTE_NAME, session.getData());
        getLogger().debug("SCS [{}] is extracted from request cookie.", session);
        return session;
    }

    /**
     * Returns the current session state attached to the passed request. If no session state attached to the request returns null.
     * Before a call to this function it necessary to call function
     * {@link com.identityblitz.scs.SCSService#extractFromUpstream(javax.servlet.http.HttpServletRequest)} first.
     * Otherwise this function returns null though the request actually has a valid SCS cookie.
     * @param req - request.
     * @return - current session state.
     */
    public static String getSCS(final Object req) {
        switch ((PLATFORM != null)?PLATFORM:determinePlatform(req)) {
            case SERVLET:
                return getServletSCS((HttpServletRequest)req);
            case NETTY_HTTP:
                return getNettySCS((SCSFullHttpRequest)req);
            case PLAY:
                return getPlaySCS((Http.Context)req);
            default:
                throw new IllegalArgumentException("wrong request type");
        }
    }

    /**
     * Sets a passed session sate as the current session state.
     * @param req - request.
     * @param newSessionState - new session state.
     */
    public static void changeSCS(final Object req, final String newSessionState) {
        switch ((PLATFORM != null)?PLATFORM:determinePlatform(req)) {
            case SERVLET:
                changeServletSCS((HttpServletRequest) req, newSessionState);
                break;
            case NETTY_HTTP:
                changeNettySCS((SCSFullHttpRequest)req, newSessionState);
                break;
            case PLAY:
                changePlaySCS((Http.Context)req, newSessionState);
                break;
            default:
                throw new IllegalArgumentException("wrong request type");
        }
    }

    private static Set<Platform> checkAvailable() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<Platform> res = new HashSet<Platform>();

        try {
            loader.loadClass("javax.servlet.http.HttpServletRequest");
            res.add(Platform.SERVLET);
        }
        catch (ClassNotFoundException e) {}
        catch (NoClassDefFoundError e) {}

        try {
            loader.loadClass("com.identityblitz.scs.glue.netty.http.SCSFullHttpRequest");
            res.add(Platform.NETTY_HTTP);
        }
        catch (ClassNotFoundException e) {}
        catch (NoClassDefFoundError e) {}

        try {
            loader.loadClass("play.mvc.Http$Context");
            res.add(Platform.PLAY);
        }
        catch (ClassNotFoundException e) {}
        catch (NoClassDefFoundError e) {}

        return Collections.unmodifiableSet(res);
    }

    private static Platform determinePlatform(final Object req) {
        if(available.contains(Platform.SERVLET) && req instanceof HttpServletRequest) {
            return Platform.SERVLET;
        }
        else if(available.contains(Platform.NETTY_HTTP) && req instanceof SCSFullHttpRequest) {
            return Platform.NETTY_HTTP;
        } if(available.contains(Platform.PLAY) && req instanceof Http.Context) {
            return Platform.PLAY;
        }
        else {
            throw new IllegalArgumentException("wrong request type");
        }
    }

    private static String getServletSCS(final HttpServletRequest request) {
        return (String)request.getAttribute(SCS_ATTRIBUTE_NAME);
    }

    private static void changeServletSCS(final HttpServletRequest request, final String newSessionState) {
        request.setAttribute(SCS_ATTRIBUTE_NAME, newSessionState);
    }

    private static String getNettySCS(final SCSFullHttpRequest request) {
        return request.getSCS();
    }

    private static void changeNettySCS(final SCSFullHttpRequest request, final String newSessionState) {
        request.changeSCS(newSessionState);
    }

    private static String getPlaySCS(final Http.Context ctx) {
        return (String)ctx.args.get(SCS_ATTRIBUTE_NAME);
    }

    private static void changePlaySCS(final Http.Context ctx, final String newSessionState) {
        ctx.args.put(SCS_ATTRIBUTE_NAME, newSessionState);
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
        String currentState = getSCS(request);
        if(currentState != null) {
            final SCSession session  = encode(currentState);
            response.addCookie(createSCSCookie(session));
            getLogger().debug("session state is stored into SCS cookie {}.", session);
            return session;
        }
        else {
            getLogger().debug("there is no session state to store in SCS cookie.");
            final Cookie expiredCookie = new Cookie(SCS_COOKIE_NAME, null);
            expiredCookie.setMaxAge(0);
            expiredCookie.setDomain(DOMAIN);
            expiredCookie.setSecure(IS_SECURE);
            expiredCookie.setHttpOnly(true);
            expiredCookie.setPath(PATH);
            response.addCookie(expiredCookie);
            return null;
        }
    }

    private static Cookie findCookie(final HttpServletRequest request, final String cookieName) {
        final Cookie[] cookies = request.getCookies();
        if(cookies == null)
            return null;
        for(Cookie cookie : cookies) {
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
        getLogger().debug("SCS cookie [name = {}, value = {}, domain = {}, " +
                "secure = {}, httpOnly = {}, path = {}] has been created.",
                new Object[]{scsCookie.getName(), scsCookie.getValue(), scsCookie.getDomain(),
                        scsCookie.getSecure(), scsCookie.isHttpOnly(), scsCookie.getPath()});
        return scsCookie;
    }

}

enum Platform {
    SERVLET,
    NETTY_HTTP,
    PLAY;

    static Platform safeValueOf(final String name) {
        if(name == null)
            return null;
        try {
            return valueOf(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }
}
