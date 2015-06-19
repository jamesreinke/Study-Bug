package controllers

import play.api._
import play.api.mvc._
import play.twirl.api.Html

object Application extends Controller {

  	val iLink = new Node.Link("Home", "fa fa-home", routes.Application.index())
	def index(a: String) = Action {
		implicit request => {
			val table = List(List("name", "boobs", "butt"), List("Shaniquoa", "C-", "A+"), List("Megan", "B+", "A-"))

			if(!Authentication.auth(request)) Ok(views.html.pages.login.core(Authentication.userForm, msg = a))
			else Ok(views.html.pages.temp.core())
		}
	}
}
