package models.problems

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel
import models.JNorm
import play.api.libs.json._


case class Problem(
	id: Long,
	contents: String,
	topic: String
	)


object Problem extends JNorm[Problem] {

	val table = "problems"

	val statements = List(
		"create table if not exists problems (id bigserial primary key, contents text, topic text);",
		"create index problems_i on problems using gin(to_tsvector('english', contents));",
		"create table if not exists problems_p (id bigserial primary key, pid bigint, pic_id bigint);",
		"create index problems_a_i on problems_p (pid, pic_id);")

	val parser = long("id") ~ str("contents") ~ str("topic") map {
		case id ~ contents ~ topic => Problem(id, contents, topic)
	}

	def toJson(p: Problem): JsObject = {
		Json.obj(
			"id" -> p.id,
			"contents" -> p.contents,
			"topic" -> p.topic
			)
	}

	def create(p: Problem): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into problems
						(contents, topic)
					values
						({contents}, {topic})
					""").on("contents" -> p.contents, "topic" -> p.topic).executeInsert()
			}
		}
	}

	def update(p: Problem): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update 
						problems p
					set
						contents = {contents}, topic = {topic}
					where
						p.id = {pid}
					""").on("contents" -> p.contents, "topic" -> p.topic, "pid" -> p.id).executeUpdate()
			}
		}
	}

	def assign(pid: Long, pic: Long): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into problems_p
						(pid, pic_id)
					values
						({pid}, {pic})
					""").on("pid" -> pid, "pic" -> pic).executeInsert()
			}
		}
	}

	def unassign(pid: Long, pic: Long): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					delete from
						problems_p p
					where
						pid = {pid}
					and
						pic_id = {pic}
					""").on("pid" -> pid, "pic" -> pic).executeUpdate()
			}
		}
	}
	/* Retrieves all pictures related to the problem */
	def getAllPictures(pid: Long): List[models.Picture] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						p.id, p.name, p.contents
					from
						pictures p, problems_p pp
					where
						p.id = pp.pic_id
					and
						pp.pid = {pid}
					""").on("pid" -> pid).as(models.Picture.parser*)
			}
		}
	}

	/* Formats the model for table presentation */
	def toTable: List[List[String]] = {
		val colNames = List("ID", "Contents", "Topic")
		val problems = getAll
		val colVals = for(problem <- problems) yield List(problem.id.toString, problem.contents, problem.topic)
		colNames +: colVals // append the column names as the first row of the matrix
	}
}