package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import models.users.User
import models.users.Authentication.{login, register}

object Authentication extends Controller {

	/* START: Helper Functions */

	private val conKey = "connected"
	private val adKey = "admin"

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
			"admin" -> default(of[Boolean], false)
			)(User.apply)(User.unapply))

	/* END: Helper Functions */

	/* START: Actions */

	def loginPost = Action {
		implicit request => {
			userForm.bindFromRequest.fold(
				formWithErrors => Redirect("/"),
				user => {
					login(user) match {
						case Some(User(id, email, password, admin)) => {
							Ok("Connected as: " + email).withSession(
								conKey -> email,
								adKey -> admin.toString)
						}
						case _ => Redirect(routes.Application.index(msg = "Invalid username or password"))
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
				formWithErrors => Redirect("/"),
				user => {
					register(user) match {
						case Some(User(id, email, password, admin)) => {
							Redirect("/").withSession(
								"id" -> id.toString,
								"connected" -> email,
								"admin" -> admin.toString)
						}
						case _ => Redirect(routes.Application.index(msg = "Email already exists"))
					}
				})
		}
	}
	/* END: Actions */

}
