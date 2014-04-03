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

