package controllers

import play.api._
import play.api.mvc._
import models.users._
import views.html.defaultpages._
import Node._

object Profile extends Controller {

	val iLink = new Link("Profile", "fa fa-user", routes.Profile.index())
	def index = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	/* User statistics */
	val sLink = new Link("Statistics", "fa fa-calculator", routes.Profile.stats())
	def stats = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}

}