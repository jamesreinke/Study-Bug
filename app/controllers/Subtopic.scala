package controllers

import play.api._
import play.api.mvc._
import models.users._
import views.html.defaultpages._
import Node._

import models.problems.Subtopic.{getById, toJson}

object Subtopic extends Controller {

	def getSubtopic(id: Long) = Action {
		implicit request => {
			println(id)
			getById(id) match {
				case Some(subtopic) => {
					println("We got a subtopic back!") 
					Ok(toJson(subtopic))
				}
				case _ => Ok("We couldn't retrieve the Json Object")
			}
		}
	}

	def delete(id: Long) = Action {
		implicit request => {
			getById(id) match {
				case Some(subtopic) => {
					models.problems.Subtopic.delete(subtopic)
					Ok("Successfully deleted the object")
				}
				case _ => Ok("We couldn't retrieve the Json Object")
			}
		}
	}

}