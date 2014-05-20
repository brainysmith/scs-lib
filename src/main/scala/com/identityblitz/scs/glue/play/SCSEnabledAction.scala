package com.identityblitz.scs.glue.play

import play.api.mvc._
import com.identityblitz.scs.{ConfigParameter, SCSService}
import scala.concurrent.Future
import scala.util.Try
import com.identityblitz.scs.error.{SCSException, SCSExpiredException, SCSBrokenException}
import play.api.libs.concurrent.Execution.Implicits._
import com.identityblitz.scs.service.ServiceProvider._
import play.api.mvc.DiscardingCookie
import play.api.mvc.Cookie
import scala.Some
import play.api.mvc.SimpleResult
import com.identityblitz.scs.LoggingUtils._
import play.mvc.Http.HeaderNames

/**
 * The Play framework action builder to add the SCS functionality to the actors from the Play application
 * written on the Scala language. Actions produced by the builder wrap the HTTP request in the special request
 * [[com.identityblitz.scs.glue.play.SCSRequest]] with two additional methods to manipulate the current SCS state.
 * To get the current SCS state use the method [[com.identityblitz.scs.glue.play.SCSRequest.getSCS]]. To change the
 * current state use [[com.identityblitz.scs.glue.play.SCSRequest.changeSCS()]].
 * If the actions gets the broken SCS cookie it returns the BAD REQUEST status and discards the SCS cookie.
 * If the action gets the expired SCS cookie the current SCS state is considered as not set.
 */
object SCSEnabledAction extends ActionBuilder[SCSRequest] {
  private final val SCS_COOKIE_NAME = service.getConfiguration
    .getString(ConfigParameter.SCS_COOKIE_NAME.key, "SCS")
  private final val DOMAIN = Option(service.getConfiguration.getString(ConfigParameter.DOMAIN.key))
  private final val IS_SECURE: Boolean = service.getConfiguration.getBoolean(ConfigParameter.IS_SECURE.key, false)
  private final val PATH = service.getConfiguration.getString(ConfigParameter.PATH.key, "/")

  private val scsService = new SCSService
  scsService.init(service.getConfiguration.getBoolean(ConfigParameter.USE_COMPRESSION.key, false))

  def invokeBlock[A](request: Request[A], block: (SCSRequest[A]) => Future[SimpleResult]): Future[SimpleResult] = {
    request match {
      case sr: SCSRequest[A] => block(sr)
      case r: Request[A] =>
        request.cookies.get(SCS_COOKIE_NAME).map(c => {
          Try[String]{
            val session = scsService.decode(c.value)
            getLogger.debug("SCS [{}] is extracted from request cookie.", session)
            session.getData}.map(s => callBlockWithState(request, block, Some(s)))
            .recover {
            case b: SCSBrokenException =>
              getLogger.error("Got broken SCS cookie: " + b.getMessage)
              Future.successful(Results.BadRequest
                .discardingCookies(DiscardingCookie(SCS_COOKIE_NAME, PATH, DOMAIN, IS_SECURE)))
            case e: SCSExpiredException =>
              getLogger.info("Got expired SCS cookie: " + e.getMessage)
              callBlockWithState(request, block)
            case s: SCSException =>
              getLogger.error(s.getMessage)
              Future.successful(Results.InternalServerError)
            case o => throw o
          }.get
        }).getOrElse(callBlockWithState(request, block))
    }
  }

  def callBlockWithState[A](request: Request[A],
                            block: (SCSRequest[A]) => Future[SimpleResult],
                            state: Option[String] = None): Future[SimpleResult] = {
    val scs = new SCSRequest(state, request)
    block(scs).map(res => scs.getSCS.map(s => {
      val session = scsService.encode(s)
      if(!Cookies.decode(res.header.headers.get(HeaderNames.SET_COOKIE).getOrElse("")).exists(_.name == SCS_COOKIE_NAME)) {
        getLogger.debug("session state is stored into SCS cookie {}.", session)
        res.withCookies(Cookie(SCS_COOKIE_NAME, session.asString, None, PATH, DOMAIN, IS_SECURE, httpOnly = true))
      }
      else {
        getLogger.debug("session state is already committed.")
        res
      }
    }).getOrElse{
      if(!Cookies.decode(res.header.headers.get(HeaderNames.SET_COOKIE).getOrElse("")).exists(_.name == SCS_COOKIE_NAME)) {
        getLogger.debug("there is no session state to store in SCS cookie.")
        res.discardingCookies(DiscardingCookie(SCS_COOKIE_NAME, PATH, DOMAIN, IS_SECURE))
      }
      else {
        getLogger.debug("session state is already committed.")
        res
      }
    })
  }
}

class SCSRequest[A](private var state: Option[String], request: Request[A]) extends WrappedRequest[A](request) {
  def getSCS = state
  def changeSCS(newState: Option[String]) = {state = newState}
}

object SCSRequest {
  def apply[A](state: Option[String], requestHeader: RequestHeader, body: A) =
    new SCSRequest(state, Request(requestHeader, body))
}
