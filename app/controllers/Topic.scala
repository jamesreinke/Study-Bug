package controllers

import play.api._
import play.api.mvc._
import models.problems.Topic.{toJson, getById, gen}

import Node._
import play.api.libs.json._
import views.html.defaultpages._
import views.html.components.topic._

object Topic extends Controller {

	/* Retrieve a topic by id; returns the topic page if there is no id supplied */
	def get(id: Long) = Action {
		implicit request => {
			id > 0 match {
				/* User is requesting a json object of our topic */
				case true => {
					getById(id) match {
						case Some(topic) => Ok(toJson(topic))
						case _ => BadRequest("Could not retrieve topic ID: " + id)
					}
				}
				case false => {
					/* Return the topics page if no id is specified */
					Ok(views.html.pages.temp.core(
						content = core(),
						exStyles = styles(),
						exJavascripts = javascripts()))
				}
			}
		}
	}

	def delete(id: Long) = Action {
		implicit request => {
			models.problems.Topic.delete(gen(id = id)) match {
				case false => Ok("Topic deleted ID: " + id)
				case true => BadRequest("Unable to delete topic with ID: " + id)
			}
		}
	}

	def create(contents: String, parent: Long) = Action {
		implicit request => {
			models.problems.Topic.create(gen(contents = contents, parent = parent)) match {
				case Some(long) => {
					val obj = Json.obj(
						"id" -> long,
						"contents" -> contents,
						"parent" -> parent
						)
				 	Ok(obj)
				}
				case _ => BadRequest("Unable to create topic Contents: " + contents)
			}
		}
	}

	def update(id: Long, contents: String, parent: Long) = Action {
		implicit request => {
			models.problems.Topic.update(gen(id = id, contents = contents, parent = parent)) match {
				case true => Ok("Successfully updated the topic ID: " + id)
				case false => BadRequest("Unable to update topic ID: " + id)
			}
		}
	}
}
