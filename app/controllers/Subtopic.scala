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

	val sform = Form(
		tuple(
			"id" -> default(of[Long],0L),
			"contents" -> default(text, ""),
			"hint" -> default(text,"")))

	val sLink = new Link("Subtopics", "", routes.Subtopic.get())
	def get = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			db.get(id) match {
				case Some(subtopic) => {
					Ok(db.toJson(subtopic))
				}
				case _ => {
					BadRequest("Subtopic not found ID: " + id)
				}
			}
		}
	}

	def getpage = Action {
		implicit request => {
			Ok(views.html.pages.temp.core(
						content = core(),
						exStyles = styles(),
						exJavascripts = javascripts()))
		}
	}
	

	def delete = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			db.delete(id) match {
				case false => Ok("Successfully deleted the object")
				case _ => BadRequest("Failed deleting subtopic ID: " + id)
			}
		}
	}

	def create = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			val item = new Subtopic(0, contents, hint)
			db.create(item) match {
				case Some(long) => {
				 	Ok(db.toJson(item))
				}
				case _ => BadRequest("Could not create the subtopic")
			}
		}
	}

	def update = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			val item = new Subtopic(id, contents, hint)
			db.update(item) match {
				case true =>{
					Ok(db.toJson(item))
				}
				case _ => BadRequest("Could not update subtopic ID: " + id)
			}
		}
	}

}