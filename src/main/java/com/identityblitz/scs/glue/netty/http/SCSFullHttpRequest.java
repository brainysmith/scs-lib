package com.identityblitz.scs.glue.netty.http;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;

public class SCSFullHttpRequest extends DefaultFullHttpRequest {
    private String session = null;

    SCSFullHttpRequest(final FullHttpRequest req, final String session) {
        super(req.getProtocolVersion(), req.getMethod(), req.getUri(), req.content(), true);
        this.headers().set(req.headers());
        this.trailingHeaders().set(req.trailingHeaders());
        this.session = session;
    }

    public String getSCS() {
        return session;
    }

    public void changeSCS(String session) {
        this.session = session;
    }
}
