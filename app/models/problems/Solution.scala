package models.problems

import models.JNorm

import anorm._
import anorm.SqlParser._

import play.api.db.DB

import play.api.Play.current

import play.api.libs.json._

case class SolutionStep(
	id: Long,
	contents: String,
	picture: String,
	problemId: Long,
	position: Int)

object Solution extends JNorm [SolutionStep] {

	val statements = List()

	val table = ""

	val aTable = ""

	def toJson(s: SolutionStep): JsObject = {
		Json.obj(
			"id" -> s.id,
			"contents" -> s.contents,
			"picture" -> s.picture,
			"problemId" -> s.problemId,
			"position" -> s.position
			)
	}

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("problemId") ~ int("position") map {
		case id ~ contents ~ picture ~ problemId ~ position => 
		SolutionStep(
		id,
		contents,
		picture,
		problemId,
		position)
	}

	def create(s: SolutionStep): Option[Long] = {
		Some(0L)
	}

	def update(s: SolutionStep): Boolean = {
		true
	}

	def getByProblemId(id: Long): List[SolutionStep] = {
		List()
	}
	
}
