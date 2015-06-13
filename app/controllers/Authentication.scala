package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.users.User
import models.users.Authentication.{login}

object Authentication extends Controller {



	val loginForm = Form(
		tuple(
			"email" -> text,
			"password" -> text))


	def loginPost = Action {

		implicit request => {
			
			val (email, password) = loginForm.bindFromRequest.get
			login(new User(id = 0, email = email, password = password, admin = false)) match {
				case Some(User(id, email, password, admin)) => {
					Ok("You Logged in.  That is a good thing!")
				}
				case _ => Ok("You fucked up...")
			}
		}

	}

	def forgetPost = Action {
		Ok("Default Forget")
	}

	def registerPost = Action {
		Ok("Default Register")
	}

}
