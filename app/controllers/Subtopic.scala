package controllers

import play.api._
import play.api.mvc._
import models.users._
import views.html.defaultpages._
import Node._

import models.problems.Subtopic.{getById, toJson, gen}
import play.api.libs.json._

object Subtopic extends Controller {

	def get(id: Long) = Action {
		implicit request => {
			id > 0 match {
				case true => {
					getById(id) match {
						case Some(subtopic) => {
							Ok(toJson(subtopic))
						}
						case _ => {
							println("We got a bad request")
							BadRequest("Subtopic not found ID: " + id)
						}
					}
				}
				case false => Ok(views.html.pages.temp.core(views.html.components.subtopic.core()))
			}
		}
	}

	def delete(id: Long) = Action {
		implicit request => {
			val failure = models.problems.Subtopic.delete(gen(id = id))
			failure match {
				case false => Ok("Successfully deleted the object")
				case _ => BadRequest("Failed deleting subtopic ID: " + id)
			}
		}
	}

	def create(contents: String, hint: String) = Action {
		implicit request => {
			models.problems.Subtopic.create(gen(0, contents, hint)) match {
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

	def update(id: Long, contents: String, hint: String) = Action {
		implicit request => {
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