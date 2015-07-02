package controllers

import play.api._
import play.api.mvc._
import play.twirl.api.Html
import views.html.components.solve._

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import models.users.ProblemSubmission.{create}
import models.users.Submission

object Account extends Controller {

	val submissionForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"answerid" -> default(of[Long], 0L)))

	def submission = Action {
		implicit request => {
			val (id, answerID) = submissionForm.bindFromRequest.get
			Authentication.getUserID(request) match {
				case Some(userID) => {
					create(new Submission(0, userID, answerID)) match {
						case Some(id) => Ok("Successfully logged answer")
						case _ => BadRequest("Unable to log answer")
					}
				}
				case _ => BadRequest("Could not retrieve the user id from the session")
			}
		}
	}
	
}
