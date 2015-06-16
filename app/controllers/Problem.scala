package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import Node._

import views.html.defaultpages._



object Problem extends Controller {

	val dLink = new Link("Problems", "", routes.Problem.database())
	def database = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	val tLink = new Link("Topics", "", routes.Problem.topics("0"))
	def topics(t: String) = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	val sLink = new Link("Subtopics", "", routes.Problem.subtopics("0"))
	def subtopics(s: String) = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
}