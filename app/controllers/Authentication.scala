package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.users.User
import models.users.Authentication.{login, register}

object Authentication extends Controller {

	val conKey = "connected"
	val adKey = "admin"

	def auth(f: Request[AnyContent]): Boolean = {
		f.session.get(conKey).map {
			user => true
		}.getOrElse {
			false
		}
	}

	def admin(f: Request[AnyContent]): Boolean = {
		f.session.get(adKey).map {
			user => true
		}.getOrElse {
			false
		}
	}

	val userForm = Form(
		mapping(
			"id" -> default(of[Long], 0L),
			"email" -> of[String],
			"password" -> of[String],
			"adin" -> default(of[Boolean], false)
			)(User.apply)(User.unapply))
	


	def loginPost = Action {
		implicit request => {
			userForm.bindFromRequest.fold(
				formWithErrors => Ok(views.html.pages.login()),
				user => {
					login(user) match {
						case Some(User(id, email, password, admin)) => {
							Ok("Connected as: " + email).withSession(
								conKey -> email,
								adKey -> admin.toString)
						}
						case _ => Ok(views.html.pages.login())
					}
				})
		}
	}

	def forgetPost = Action {
		Ok("Default Forget")
	}

	def registerPost = Action {
		implicit request => {
			userForm.bindFromRequest.fold(
				formWithErrors => Ok(views.html.pages.login()),
				user => {
					register(user) match {
						case Some(User(id, email, password, admin)) => {
							Ok("Connected as: " + email).withSession(
								"connected" -> email,
								"admin" -> admin.toString)
						}
						case _ => Ok(views.html.pages.login())
					}
				})
		}
	}

}
