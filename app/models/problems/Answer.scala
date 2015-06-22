package models.problems

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel
import models.JNorm
import play.api.libs.json._

case class Answer(
	id: Long,
	contents: String,
	picture: String,
	pid: Long,
	correct: Boolean
	)

object Answer extends JNorm[Answer] {

	val table = "answers"

	val aTable = "answers_a"

	val statements = List(
		"create table if not exists answers (id bigserial primary key, contents text, picture varchar, pid bigint, correct boolean);",
		"create index answers_i on answers (pid);")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("pid") ~ bool("correct") map {
		case id ~ contents ~ picture ~ pid ~ correct => Answer(id, contents, picture, pid, correct)
	}

	def toJson(a: Answer): JsObject = {
		Json.obj(
			"id" -> a.id,
			"contents" -> a.contents,
			"picture" -> a.picture,
			"pid" -> a.pid,
			"correct" -> a.correct)
	}

	def create(a: Answer): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into answers
						(id, contents, picture, pid, correct)
					values
						({id}, {contents}, {picture}, {pid}, {correct})
					""").on("id" -> a.id, "contents" -> a.contents, "picture" -> a.picture, "pid" -> a.pid, "correct" -> a.correct).executeInsert()
			}
		}
	}

	def update(a: Answer): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update 
						answers a
					set
						contents = {contents}, picture = {picture}, pid = {pid}, correct = {correct}
					where
						s.id = {sid}
					""").on("id" -> a.id, "contents" -> a.contents, "picture" -> a.picture, "pid" -> a.pid, "correct" -> a.correct).executeUpdate()
			}
		}
	}

	def getByProblemId(id: Long): List[Answer] = {
		DB.withConnection {
			implicit session => {
				SQL(

					"""
					select
						*
					from
						answers a
					where
						a.pid = {id}
					""").on("id" -> id).as(parser*)
			}
		}
	}
}