package controllers

import play.api._
import play.api.mvc._

import models.problems.{Topic => db}

import Node._
import play.api.libs.json._
import views.html.defaultpages._
import views.html.components.topic._

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import views.html.components.topic._

object Topic extends Controller {

	import models.problems.Topic

	val tForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"contents" -> default(text, ""),
			"parent" -> default(of[Long],0L)))

	def getPage = Action {
		implicit request => {
			Ok(views.html.pages.temp.core(
				content = core(),
				exStyles = styles(),
				exJavascripts = javascripts()))
		}
	}


	/* POST - retrieves topic by id */
	def get = Action {
		implicit request => {
			val (id, contents, parent) = tForm.bindFromRequest.get
			db.get(id) match {
				case Some(topic) => Ok(db.toJson(topic))
				case _ => BadRequest("Unable able to retrieve topic ID: " + id)
			}
		}
	}

	/* POST - deletes a topic by id */
	def delete = Action {
		implicit request => {
			val (id, contents, parent) = tForm.bindFromRequest.get
			db.delete(id) match {
				case true => Ok("Topic deleted ID: " + id)
				case false => BadRequest("Unable to delete topic with ID: " + id)
			}
		}
	}

	/* POST - generates a topic */
	def create = Action {
		implicit request => {
			val (id, contents, parent) = tForm.bindFromRequest.get
			val item = new Topic(0, contents, parent)
			db.create(item) match {
				case Some(long) => Ok(db.toJson(new Topic(long, contents, parent)))
				case _ => BadRequest("Unable to create topic Contents: " + contents)
			}
		}
	}

	/* POST - updates a topic */
	def update = Action {
		implicit request => {
			val (id, contents, parent) = tForm.bindFromRequest.get
			val item = new Topic(id, contents, parent)
			db.update(item) match {
				case true => Ok(db.toJson(item))
				case false => BadRequest("Unable to update topic ID: " + id)
			}
		}
	}
}
