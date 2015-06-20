package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import Node._

import views.html.defaultpages._

import models.problems._

import Authentication.{auth, admin}

import models.problems.{Subtopic => SubtopicModel}



object Problem extends Controller {

	val dLink = new Link("Problems", "fa fa-database", routes.Problem.database())
	def database = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	val tLink = new Link("Topics", "", routes.Topic.get())
	val sLink = new Link("Subtopics", "", routes.Subtopic.get())
	val cLink = new LinkedLinks("Categories", "fa fa-university", List(tLink, sLink))

}