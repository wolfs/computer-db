package controllers

import play.api.mvc._

/**
 * @author wolfs
 */
trait Secured {

  def username(request: RequestHeader) = UsernameCookie.decodeFromCookie(request.cookies.get(UsernameCookie.COOKIE_NAME)).map { _.username }

  def onUnauthorized(request: RequestHeader) = Results.Unauthorized

  def withAuth(f: String => Request[AnyContent] => Result): EssentialAction = withAuth(BodyParsers.parse.anyContent)(f)

  def withAuth[A](bodyParser : BodyParser[A])(f : String => Request[A] => Result): EssentialAction =
    withAuthAndResult(bodyParser)(f)(addUsernameCookie)

  def withAuthAndResult[A](bodyParser : BodyParser[A])(f : String => Request[A] => Result)(resultAction: (String, Result) => Result): EssentialAction = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(bodyParser)(request => resultAction(user, f(user)(request)))
    }
  }

  def withAuthAndLogout[A](bodyParser : BodyParser[A])(f : String => Request[A] => Result): EssentialAction =
    withAuthAndResult(bodyParser)(f) { (user, result) =>
      result.discardingCookies(UsernameCookie.discard)
    }

  def withAuthAndLogout(f : String => Request[AnyContent] => Result): EssentialAction =
    withAuthAndLogout(BodyParsers.parse.anyContent)(f)

  def addUsernameCookie(user: String, result: Result): Result = {
    result.withCookies(UsernameCookie.encodeAsCookie(Some(Username(user))))
  }

  def authenticated[A](bodyParser : BodyParser[A])(f : Request[A] => Result): EssentialAction = withAuth(bodyParser)((user) => f)
  def authenticated(f : Request[AnyContent] => Result): EssentialAction = withAuth((user) => f)
  def authenticated(f :  => Result): EssentialAction = authenticated(_ => f)

}
