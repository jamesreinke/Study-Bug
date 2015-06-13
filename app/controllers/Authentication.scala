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

	val loginForm = Form(
		mapping(
			"id" -> default(of[Long], 0L),
			"email" -> of[String],
			"password" -> of[String],
			"admin" -> default(of[Boolean], false)
			)(User.apply)(User.unapply))

	val registerForm = Form(
		mapping(
			"id" -> default(of[Long], 0L),
			"email" -> of[String],
			"password" -> tuple(
				"first" -> text,
				"second" -> text
				).verifying(
					"Passwords don't match", p => p._1 == p._2
				).transform(
					{ case (main, confirm) => main },
					( main: String) => ("", "") ),
			"admin" -> default(of[Boolean], false))(User.apply)(User.unapply))

	/* END: Helper Functions */

	/* START: Actions */

	def loginPost = Action {
		implicit request => {
			loginForm.bindFromRequest.fold(
				formWithErrors => Ok(views.html.pages.login(formWithErrors, registerForm)),
				user => {
					login(user) match {
						case Some(User(id, email, password, admin)) => {
							Ok("Connected as: " + email).withSession(
								conKey -> email,
								adKey -> admin.toString)
						}
						case _ => {
							Ok(views.html.pages.login(loginForm, registerForm))
						}
					}
				})
		}
	}

	def forgetPost = Action {
		Ok("Default Forget")
	}

	def registerPost = Action {
		implicit request => {
			loginForm.bindFromRequest.fold(
				formWithErrors => Ok(views.html.pages.login(loginForm, formWithErrors)),
				user => {
					register(user) match {
						case Some(User(id, email, password, admin)) => {
							Ok("Connected as: " + email).withSession(
								"connected" -> email,
								"admin" -> admin.toString)
						}
						case _ => Ok(views.html.pages.login(loginForm, registerForm))
					}
				})
		}
	}
	/* END: Actions */

}
