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



object Problem extends Controller {

	val dLink = new Link("Problems", "fa fa-database", routes.Problem.database())
	def database = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}
	val tLink = new Link("Topics", "", routes.Problem.topic())
	val sLink = new Link("Subtopics", "", routes.Problem.subtopic())
	val cLink = new LinkedLinks("Categories", "fa fa-university", List(tLink, sLink))

	/* Topic form and GET/POST handler */
	val tForm = Form(
		mapping(
			"id" -> default(of[Long], 0L),
			"contents" -> of[String],
			"parent" -> default(of[Long], 0L))(Topic.apply)(Topic.unapply))
	def topic() = Action {
		implicit request => {
			auth(request) match { // TODO change to matching admins not authenticated
				case false => Redirect("/")
				case true => {
					Topic.getById(0) match {
						case Some(topic) => Ok(views.html.pages.temp.core(views.html.components.topic.core(topic)))
						case _ => Ok(views.html.pages.temp.core(views.html.components.topic.core()))
					}
				}
			}
		}
	}
	def topicPost = Action {
		implicit request => {
			tForm.bindFromRequest.fold(
				formWithErrors => Ok(views.html.pages.temp.core(views.html.components.topic.core(errMsg = "Error"))),
				topic => {
					Topic.create(topic) match {
						case Some(id) => Ok(views.html.pages.temp.core(views.html.components.topic.core(topic = topic)))
						case _ => Ok(views.html.pages.temp.core(views.html.components.topic.core(errMsg = "Topic Already Exists", topic = topic)))
					}
				})
		}
	}
	/* Subtopic form and GET/POST handler */
	val sForm = Form(
		mapping(
			"id" -> default(of[Long], 0L),
			"contents" -> of[String],
			"hint" -> default(of[String], ""))(Subtopic.apply)(Subtopic.unapply))
	def subtopic() = Action {
		implicit request => {
			auth(request) match { // TODO change to matching admins not authenticated
				case false => Redirect("/")
				case true => {
					Subtopic.getById(0) match {
						case Some(subtopic) => Ok(views.html.pages.temp.core(views.html.components.subtopic.core(subtopic)))
						case _ => Ok(views.html.pages.temp.core(views.html.components.subtopic.core()))
					}
				}
			}
		}
	}
	def subtopicPost = Action {
		implicit request => {
			sForm.bindFromRequest.fold(
				formWithErrors => Ok(views.html.pages.temp.core(views.html.components.subtopic.core(errMsg = "Error"))),
				subtopic => {
					Subtopic.create(subtopic) match {
						case Some(id) => Ok(views.html.pages.temp.core(views.html.components.subtopic.core(subtopic = subtopic)))
						case _ => Ok(views.html.pages.temp.core(views.html.components.subtopic.core(errMsg = "Subtopic Already Exists", subtopic = subtopic)))
					}
				})
		}
	}
}