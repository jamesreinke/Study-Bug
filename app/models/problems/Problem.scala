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

	val aTable = "problems_a"

	val statements = List(
		"create table if not exists problems (id bigserial primary key, contents text, topic bigint);",
		"create index if problems_a on probelms gin(to_tsvector('english', contents));",
		"create table if not exists problems_pictures (id bigserial primary key, contents varchar, pid bigint);",
		"create index if problems_pictures_i on problems_pictures (pid);")

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
						(id, contents, topic)
					values
						({contents}, {topic})
					""").on("id" -> p.id, "topic" -> p.topic).executeInsert()
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
}