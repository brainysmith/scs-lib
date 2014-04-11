SCS-LIB
=======

The Java implementation of Secure Cookie Session (SCS). The implementation conforms
RFC 6896 (https://tools.ietf.org/html/rfc6896). This library provides:

 * Interface representing SCS and its implementation;
 * Service providing basic functions to encode and decode SCS value and few functions to easy incorporate SCS in
   Java applications using servlets;
 * SPI interfaces which must be implemented to provide configuration and cryptographic services. Inside library there are
   two basic implementations of this services;
 * Servlet filter to easy embed SCS in Java applications using servlets.

Usage
=====

There are three pre-built instruments to incorporate the SCS into applications based on Java Servlet, Netty-Http, Play.

Java Servlet
------------
 There is a servlet filter **com.identityblitz.scs.glue.servlet.SCSFilter** to built the SCS into the application.
 The filter should be configured in the Web application deployment descriptor, web.xml. The filter must be placed
 in the chains before any filters or servlets that access SCS. The example of filter configuration in web.xml is
 given below.
 ```
    <filter>
        <filter-name>SCS</filter-name>
        <filter-class>com.identityblitz.scs.glue.servlet.SCSFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>SCS</filter-name>
        <url-pattern>*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>FORWARD</dispatcher>
        <dispatcher>INCLUDE</dispatcher>
    </filter-mapping>
 ```
 To get access to SCS from code there are two static methods in the class **com.identityblitz.scs.SCSService**.
  * SCSService.getSCS(final Object req) - to get the current SCS value.
  * SCSService.changeSCS(final Object req, final String newSCSValue) - to set new SCS value.

 *IMPORTANT*
 As the SCS is stored in the cookie and the storing happens at the end of the filter, after completion of the call to
 **doFilter**, it is important the response would not be committed. If the filter found the response is committed the
 following message is printed to the log.
  *Response is already committed so SCS cookie will not be set and all session state changes made during processing
  the current request will be lost.*

Netty-Http
----------
 To incorporate SCS into the application it is enough to add the handler **com.identityblitz.scs.glue.netty.http.SCSHandler**
 to the pipeline after HttpRequestDecoder, HttpObjectAggregator and HttpResponseEncoder. Below the snippet of the code to
 add SCS handler.
 ```
    import com.identityblitz.scs.glue.netty.http.SCSHandler;

    ...

    pipeline.addLast("decoder", new HttpRequestDecoder());
    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("scs", new SCSHandler());
 ```
 To get access to SCS from code using the same methods as in the case with Java Servlet.

Play
----
 The annotation **com.identityblitz.scs.glue.play.SCSEnabled** adds SCS functionality to the Play actor or to the controller.
 The example of adding SCS to Play actor is given below.
 ```
    @SCSEnabled
    public static Result login() {
        return ok(login.render(loginForm));
    }
 ```
 For access to the SCS also using the same methods, but instead of the request HTTP context used. The example of obtaining
 of the SCS is below.
 ```
    SCSService.getSCS(ctx());
 ```