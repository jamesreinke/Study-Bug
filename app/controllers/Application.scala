package controllers

import play.api._
import play.api.mvc._
import play.twirl.api.Html
import views.html.components.solve._

object Application extends Controller {

	def index(a: String) = Action {
		implicit request => {
			if(!Authentication.auth(request)) Ok(views.html.pages.login.core(Authentication.userForm, msg = a))
			else Ok(views.html.pages.temp.core(
						content = core(),
						exStyles = styles(),
						exJavascripts = javascripts()))
		}
	}
}
