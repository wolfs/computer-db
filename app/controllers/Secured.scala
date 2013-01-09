package controllers

import play.api.mvc._

/**
 * @author wolfs
 */
trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Unauthorized

  def withAuth(f: String => Request[AnyContent] => Result): Action[(Action[AnyContent], AnyContent)] = withAuth(BodyParsers.parse.anyContent)(f)

  def withAuth[A](bodyParser : BodyParser[A])(f : String => Request[A] => Result): Action[(Action[A], A)] =
    withAuthAndResult(bodyParser)(f)(addUsernameCookie)

  def withAuthAndResult[A](bodyParser : BodyParser[A])(f : String => Request[A] => Result)(resultAction: (String, Result) => Result): Action[(Action[A], A)] = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(bodyParser)(request => resultAction(user, f(user)(request)))
    }
  }

  def withAuthAndLogout[A](bodyParser : BodyParser[A])(f : String => Request[A] => Result): Action[(Action[A], A)] =
    withAuthAndResult(bodyParser)(f) { (user, result) =>
      result.discardingCookies(UsernameCookie.discard)
    }

  def withAuthAndLogout(f : String => Request[AnyContent] => Result): Action[(Action[AnyContent], AnyContent)] =
    withAuthAndLogout(BodyParsers.parse.anyContent)(f)

  def addUsernameCookie(user: String, result: Result): Result = {
    result.withSession(Security.username -> user).withCookies(UsernameCookie.encodeAsCookie(Some(Username(user))))
  }

  def authenticated[A](bodyParser : BodyParser[A])(f : Request[A] => Result): Action[(Action[A], A)] = withAuth(bodyParser)((user) => f)
  def authenticated(f : Request[AnyContent] => Result): Action[(Action[AnyContent], AnyContent)] = withAuth((user) => f)
  def authenticated(f :  => Result): Action[(Action[AnyContent], AnyContent)] = authenticated(_ => f)

}
