package controllers

import Node._

import play.api._
import play.api.mvc._

import models.users._
import models.problems.{Subtopic => db}

import views.html.defaultpages._

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import views.html.components.subtopic._

object Subtopic extends Controller {

import models.problems.Subtopic

	val sForm = Form(
		tuple(
			"id" -> default(of[Long],0L),
			"contents" -> default(text, ""),
			"hint" -> default(text,"")))

	/* GET - Loads subtopic database page */
	def getPage = Action {
		implicit request => {
			Ok(views.html.pages.temp.core(
						content = core(),
						exStyles = styles(),
						exJavascripts = javascripts()))
		}
	}
	/* POST - Retrieves a subtopic item by id*/
	def get = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sForm.bindFromRequest.get
			db.get(id) match {
				case Some(subtopic) => Ok(db.toJson(subtopic))
				case _ => BadRequest("Subtopic not found ID: " + id)
			}
		}
	}
	/* POST - deletes an item by ID */
	def delete = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sForm.bindFromRequest.get
			db.delete(id) match {
				case 1 => Ok("Successfully deleted subtopic ID: " + id)
				case _ => BadRequest("Failed deleting subtopic ID: " + id)
			}
		}
	}
	/* POST - Generates a new subtopic */
	def create = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sForm.bindFromRequest.get
			val item = new Subtopic(0, contents, hint)
			db.create(item) match {
				case Some(long) => Ok(db.toJson(new Subtopic(long, contents, hint)))
				case _ => BadRequest("Could not create the subtopic")
			}
		}
	}
	/* POST - Updates a subtopic */
	def update = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sForm.bindFromRequest.get
			val item = new Subtopic(id, contents, hint)
			db.update(item) match {
				case 1 => Ok(db.toJson(item))
				case _ => BadRequest("Could not update subtopic ID: " + id)
			}
		}
	}

}