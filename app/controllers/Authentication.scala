package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.users.User

object Authentication extends Controller {

	def loginPost = Action {

		implicit request => {
			request.body.asFormUrlEncoded match {
				case Some(s: Map[String, Seq[String]]) => {
					println(s.getOrElse("email", "not what we wanted"))
					println(s.getOrElse("password", "not what we wanted"))
				}
			}
			println(request.body.asFormUrlEncoded)
			Ok(request.body.asFormUrlEncoded.toString)
		}

	}

	def forgetPost = Action {
		Ok("Default Forget")
	}

	def registerPost = Action {
		Ok("Default Register")
	}

}
