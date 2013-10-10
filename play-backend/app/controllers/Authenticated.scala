package controllers

import play.api.mvc._
import play.api.mvc.Security.AuthenticatedRequest
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object AuthRequestType {
  type AuthRequest[A] = AuthenticatedRequest[A, String]
}

abstract class BaseAuthenticatedAction extends ActionBuilder[AuthRequestType.AuthRequest] {
  def username(request: RequestHeader) = UsernameCookie.decodeFromCookie(request.cookies.get(UsernameCookie.COOKIE_NAME)).map { _.username }  
  def onUnauthorized() = Future.successful(Results.Unauthorized)
}

object AuthenticatedAction extends BaseAuthenticatedAction {

  def addUsernameCookie(user: String, result: SimpleResult): SimpleResult = {
    result.withCookies(UsernameCookie.encodeAsCookie(Some(Username(user))))
  }

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A, String] => Future[SimpleResult]): Future[SimpleResult] = { 
    username(request).map { user =>
      block(new AuthenticatedRequest(user, request)) map { addUsernameCookie(user, _) }
    } getOrElse onUnauthorized
  }
}

object AuthenticatedActionWithLogout extends BaseAuthenticatedAction {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A, String] => Future[SimpleResult]): Future[SimpleResult] = { 
    username(request).map { user =>
      block(new AuthenticatedRequest(user, request)) map { _.discardingCookies(UsernameCookie.discard) }
    } getOrElse onUnauthorized
  }
}
