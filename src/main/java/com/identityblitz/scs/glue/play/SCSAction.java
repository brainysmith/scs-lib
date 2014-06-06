package com.identityblitz.scs.glue.play;

import com.identityblitz.scs.ConfigParameter;
import com.identityblitz.scs.SCSService;
import com.identityblitz.scs.SCSession;
import com.identityblitz.scs.error.SCSExpiredException;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

import static com.identityblitz.scs.LoggingUtils.getLogger;
import static com.identityblitz.scs.service.ServiceProvider.service;

/**
 * This Play framework action allows to add Secure Cookie Session (SCS) functionality to
 * actions of the Play framework application. The action should be built into actions chain before any actions that use SCS.
 * To get current session state it is necessary to use the function
 * {@link com.identityblitz.scs.SCSService#getSCS(Object)}
 * and to change the current session state - the function
 * {@link com.identityblitz.scs.SCSService#changeSCS(Object, String)}.
 * The filter has only one boolean option to configure <b>com.blitz.scs.useCompression</b>. This option turns off/on
 * using of compression session state. Default value is not to use compression.
 */
public class SCSAction extends Action.Simple {
    private static final String SCS_COOKIE_NAME = service().getConfiguration()
            .getString(ConfigParameter.SCS_COOKIE_NAME.key(), "SCS");
    private static final String DOMAIN = service().getConfiguration()
            .getString(ConfigParameter.DOMAIN.key());
    private static final boolean IS_SECURE = service().getConfiguration()
            .getBoolean(ConfigParameter.IS_SECURE.key(), false);
    private static final String PATH = service().getConfiguration()
            .getString(ConfigParameter.PATH.key(), "/");

    private static final SCSService scsService;
    static {
        scsService = new SCSService();
        scsService.init(service().getConfiguration()
                .getBoolean(ConfigParameter.USE_COMPRESSION.key(), false), null);
    }

    @Override
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        final Http.Cookie scsCookie = ctx.request().cookie(SCS_COOKIE_NAME);
        if(scsCookie != null) {
            try {
                final SCSession session = scsService.decode(scsCookie.value());
                getLogger().debug("SCS [{}] is extracted from request cookie.", session);
                SCSService.changeSCS(ctx, session.getData());
            }
            catch(SCSExpiredException e) {}
        }
        final F.Promise<SimpleResult> resultPromise = delegate.call(ctx);

        final String state = SCSService.getSCS(ctx);
        if(state != null) {
            final SCSession session = scsService.encode(state);
            getLogger().debug("session state is stored into SCS cookie {}.", session);
            ctx.response().setCookie(SCS_COOKIE_NAME, session.asString(), null, PATH, DOMAIN, IS_SECURE, true);
        }
        else {
            getLogger().debug("there is no session state to store in SCS cookie.");
            ctx.response().discardCookie(SCS_COOKIE_NAME);
        }

        return resultPromise;
    }

}
