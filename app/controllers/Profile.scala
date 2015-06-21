package controllers

import play.api._
import play.api.mvc._

import views.html.defaultpages._

import Node._

import Authentication.{auth, admin}

object Profile extends Controller {

	def index = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	/* User statistics */
	def stats = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}

}