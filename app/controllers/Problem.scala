package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import Node._

import views.html.defaultpages._

import Authentication.{auth, admin}

import views.html.components.problem._

import play.api.libs.json._

object Problem extends Controller {

import models.problems.Problem
import models.Picture
import java.io.File

	def database = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}

	/* GET - Loads subtopic database page */
	def getPage = Action {
		implicit request => {
			Ok(views.html.pages.temp.core(
						content = core(),
						exStyles = styles(),
						exJavascripts = javascripts()))
		}
	}
	/* Recursively find a unique filename with an integer prefix tag */
	private def unique(filename: String, n: Int  = 0): String = {
		val filepath = "public/images/" + n + "-" + filename
		val f = new File(filepath)
		if( !f.exists ) filepath
		else unique(filename, n + 1)
	}

	/* POST - upload problem related pictures 
		We use a path variable to accomidate the dropbox module functionality
	*/
	def pictures(id: Long) = Action(parse.multipartFormData) {
		implicit request => {
			request.body.file("pic").map {
				pic => {
					val filepath = unique(pic.filename) // generate a unique filename for the picture
					val f = new File(filepath)
					pic.ref.moveTo(f)
					Picture.create(new Picture(0, pic.filename, filepath)) match {
						case Some(long) => {
							models.problems.Problem.assign(id, long) match {
								case Some(long) => Ok(Picture.toJson(new Picture(long, pic.filename, filepath)))
								case _ => BadRequest("Error while assigning picture to problem")
							}
						}
						case _ => BadRequest("Error while adding picture to the database")
					}
				}
				}.getOrElse {
					BadRequest("Could not upload file")
			}
		}
	}

	val pictureDeleteForm = Form(
		tuple(
			"pid" -> default(of[Long], 0L),
			"pic" -> default(of[Long],0L)))

	def picDelete =  Action {
		implicit request => {
			val (pid, pic) = pictureDeleteForm.bindFromRequest.get
			Picture.get(pic) match {
				case Some(Picture(id, name, path)) => {
					new File(path).delete()
					models.problems.Problem.unassign(pid, pic)
					Picture.delete(id)
					Ok("Sucessfully deleted picture name: " + name)
				}
				case _ => BadRequest("Error while retrieving filepath from picture id")
			}	
		}
	}

	val pForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"contents" -> default(text, ""),
			"topic" -> default(text, "")))

	def get = Action {
		implicit request => {
			val (id, contents, topic) = pForm.bindFromRequest.get
			models.problems.Problem.get(id) match {
				case Some(problem) => Ok(models.problems.Problem.toJson(problem))
				case _ => BadRequest("Error retrieving problem ID: " + id)
			}
		}
	}

	def create = Action {
		implicit request => {
			val (id, contents, topic) = pForm.bindFromRequest.get
			models.problems.Problem.create(new Problem(id, contents, topic)) match {
				case Some(long) => {
					Ok(models.problems.Problem.toJson(new Problem(long, contents, topic)))
				}
				case _ => BadRequest("Error while inserting new problem into database")
			}
		}
	}

	def getPictures = Action {
		implicit request => {
			val (id, contents, topic) = pForm.bindFromRequest.get
			val pics = models.problems.Problem.getAllPictures(id) // list of Pictures
			val jsonArray = Json.obj("pictures" -> pics.map(x => models.Picture.toJson(x)))
			Ok(jsonArray)
		}
	}

}