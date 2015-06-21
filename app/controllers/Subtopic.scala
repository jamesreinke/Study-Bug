package controllers

import play.api._
import play.api.mvc._
import models.users._
import views.html.defaultpages._
import Node._

import models.problems.Subtopic.{getById, toJson, gen}
import play.api.libs.json._

import views.html.components.subtopic._

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._


object Subtopic extends Controller {


	val sform = Form(
		tuple(
			"id" -> default(of[Long],0L),
			"contents" -> default(text, ""),
			"hint" -> default(text,"")))

	val sLink = new Link("Subtopics", "", routes.Subtopic.get())
	def get = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			getById(id) match {
				case Some(subtopic) => {
					Ok(toJson(subtopic))
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
			models.problems.Subtopic.delete(gen(id = id)) match {
				case false => Ok("Successfully deleted the object")
				case _ => BadRequest("Failed deleting subtopic ID: " + id)
			}
		}
	}

	def create = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			models.problems.Subtopic.create(gen(id, contents, hint)) match {
				case Some(long) => {
					val obj = Json.obj(
						"id" -> long,
						"contents" -> contents,
						"hint" -> hint
						)
				 	Ok(obj)
				}
				case _ => BadRequest("Could not create the subtopic")
			}
		}
	}

	def update = Action {
		implicit request => {
			val (id: Long, contents: String, hint: String) = sform.bindFromRequest.get
			models.problems.Subtopic.update(gen(id, contents, hint)) match {
				case true =>{
					val obj = Json.obj(
						"id" -> id,
						"contents" -> contents,
						"hint" -> hint)
					Ok(obj)
				}
				case _ => BadRequest("Could not update subtopic ID: " + id)
			}
		}
	}

}