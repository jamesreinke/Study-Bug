package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._



object Problem extends Controller {


	def database = Action {
		implicit request => Ok("Database")
	}

	def topics(t: String) = Action {
		implicit request => Ok("Topics")
	}
}