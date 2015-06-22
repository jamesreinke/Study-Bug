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
	topic: String,
	pictures: List[String],
	answers: List[Answer]
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
		case id ~ contents ~ topic => Problem(id, contents, topic, List(), List())
	}

	def toJson(p: Problem): JsObject = {
		Json.obj(
			"id" -> p.id,
			"contents" -> p.contents,
			"topic" -> p.topic,
			"pictures" -> Json.toJson(p.pictures),
			"answers" -> Json.toJson(p.answers.map(x => Answer.toJson(x))))
	}

	def create(p: Problem): Option[Long] = {
		DB.withConnection {
			implicit session => {
				val pid = SQL(
					"""
					insert into problems
						(id, contents, topic)
					values
						({contents}, {topic})
					""").on("id" -> p.id, "topic" -> p.topic).executeInsert()
				for(pic <- p.pictures){
					SQL(
						"""
						insert into problems_pictures
							(contents, pid)
						values
							({contents}, {pid})
						""").on("contents" -> pic, "pid" -> pid).executeInsert()
				}
				for(answer <- p.answers) Answer.create(answer)
				pid
			}
		}
	}

	def update(p: Problem): Int = {
		DB.withConnection {
			implicit session => {
				val updates = SQL(
					"""
					update 
						problems p
					set
						contents = {contents}, topic = {topic}
					where
						p.id = {pid}
					""").on("contents" -> p.contents, "topic" -> p.topic, "pid" -> p.id).executeUpdate()
				for(pic <- p.pictures){
					SQL(
						"""
						update
							problems_pictures
						set
							contents = {contents}, pid = {pid}
						""").on("contents" -> pic, "pid" -> p.id).executeUpdate()
				}
				for(answer <- p.answers) Answer.update(answer)
				updates // # of modified rows
			}
		}
	}
}