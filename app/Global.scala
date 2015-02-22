/**
 * Copyright 2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import controllers.CustomRoutesService
import java.lang.reflect.Constructor
import securesocial.core.RuntimeEnvironment
import service.{ SecureSocialEventListener, InMemoryUserService }
import scala.collection.immutable.ListMap
import securesocial.core.providers.GitHubProvider
import service.SecureSocialEventListener
import securesocial.controllers.ViewTemplates
import models.User
import play.api.data.Form
import play.api.mvc.RequestHeader
import play.api.i18n.Lang
import play.twirl.api.Html
import play.api.mvc.Flash
import securesocial.controllers.RegistrationInfo
import securesocial.controllers.ChangeInfo

object Global extends play.api.GlobalSettings {
  object MyViewTemplates {
    /**
     * The default views.
     */
    class Default(env: RuntimeEnvironment[_]) extends ViewTemplates {
      implicit val implicitEnv = env
      override def getLoginPage(form: Form[(String, String)],
                                msg: Option[String] = None)(implicit request: RequestHeader, lang: Lang): Html = {
        views.html.login(null)
//        Html("Login Page")
      }
      override def getSignUpPage(form: Form[RegistrationInfo], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
        securesocial.views.html.Registration.signUp(form, token)
      }
      override def getStartSignUpPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
        securesocial.views.html.Registration.startSignUp(form)
      }
      override def getStartResetPasswordPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
        securesocial.views.html.Registration.startResetPassword(form)
      }
      override def getResetPasswordPage(form: Form[(String, String)], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
        securesocial.views.html.Registration.resetPasswordPage(form, token)
      }
      override def getPasswordChangePage(form: Form[ChangeInfo])(implicit request: RequestHeader, lang: Lang): Html = {
        securesocial.views.html.passwordChange(form)
      }
      def getNotAuthorizedPage(implicit request: RequestHeader, lang: Lang): Html = {
//        views.html.login(null)
        Html("no authorized")
      }
    }
  }
  /**
   * The runtime environment for this sample app.
   */
  object MyRuntimeEnvironment extends RuntimeEnvironment.Default[User] {
    override lazy val routes = new CustomRoutesService()
    override lazy val userService: InMemoryUserService = new InMemoryUserService()
    override lazy val eventListeners = List(new SecureSocialEventListener())
//    override lazy val viewTemplates: ViewTemplates = new ViewTemplates.Default(this)
    override lazy val viewTemplates: ViewTemplates = new MyViewTemplates.Default(this)
    override lazy val providers = ListMap(
      include(new GitHubProvider(routes, cacheService, oauth2ClientFor(GitHubProvider.GitHub))))
    //      ,include(new GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))),
    //      include(new UsernamePasswordProvider[DemoUser](userService, avatarService, viewTemplates, passwordHashers)))
  }

  /**
   * An implementation that checks if the controller expects a RuntimeEnvironment and
   * passes the instance to it if required.
   *
   * This can be replaced by any DI framework to inject it differently.
   *
   * @param controllerClass
   * @tparam A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(MyRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}
