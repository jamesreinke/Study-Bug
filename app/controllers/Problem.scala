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

object Problem extends Controller {

import models.problems.Problem

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
	/* POST - upload problem related pictures */
	def pictures(id: Long) = Action(parse.multipartFormData) {
		import java.io.File
		val rand = scala.util.Random
		implicit request => {
			request.body.file("pic").map {
				pic => {
					val seed = rand.nextInt()
					val filename = seed + "-" + pic.filename
					val filepath = "public/images/" + filename
					val f = new File(filepath)
					if( !f.exists ) pic.ref.moveTo(f)
					Ok(pic.filename)
				}
				}.getOrElse {
					BadRequest("Could not upload file")
			}
		}
	}

}