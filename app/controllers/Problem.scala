package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import Node._

import views.html.defaultpages._



object Problem extends Controller {

	val dLink = new Link("Problems", "fa fa-database", routes.Problem.database())
	def database = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	val tLink = new Link("Topics", "fa fa-university", routes.Problem.topics())
	def topics = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	val sLink = new Link("Subtopics", "fa fa-university", routes.Problem.subtopics())
	def subtopics = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
}