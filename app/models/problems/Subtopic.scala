package models.problems


import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel

import play.api.libs.json._

case class Subtopic(
	id: Long,
	contents: String,
	hint: String)

object Subtopic extends AnormModel {

	def toJson(s: Subtopic): JsObject = {
		Json.obj(
			"id" -> s.id,
			"contents" -> s.contents,
			"hint" -> s.hint
			)
	}

	type T = Subtopic

	val tableStatements = List(
		"create table if not exists subtopics (id bigserial primary key, contents text, hint text);",
		"create index subtopics_i on subtopics using gin(to_tsvector('english', contents));",
		"create table if not exists subtopics_a (id bigserial primary key, subtopic_id bigint, step_id bigint);",
		"create index subtopics_ai on subtopics_a (subtopic_id, step_id);")

	val parser = long("id") ~ str("contents") ~ str("hint") map {
		case id ~ contents ~ hint => Subtopic(id, contents, hint)
	}

	def create(s: Subtopic): Option[Long] = {
		println("creating new subtopic")
		println(s)
		if( !exists(s) ) {
			println("The subtopic does not exist")
			DB.withConnection { 
				implicit session => {
					SQL(
						"""
						insert into subtopics
							(contents, hint)
						values
							({contents}, {hint})
						""").on("contents" -> s.contents, "hint" -> s.hint).executeInsert()
				}
			}
		}
		else None
	}

	def update(s: Subtopic): Boolean = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update 
						subtopics s
					set
						contents = {contents}, hint = {hint}
					where
						s.id = {sid}
					""").on("sid" -> s.id, "contents" -> s.contents, "hint" -> s.hint).execute()
			}
		}
	}

	/* Assign a subtopic to a solution step */
	def assign(sid: Long, ssid: Long): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into subtopics_a
						(subtopic_id, step_id)
					values
						({sid}, {ssid})
					""").on("sid" -> sid, "ssid" -> ssid).executeInsert()
			}
		}
	}

	/* Checks whether a subtopic already exists */
	def exists(s: Subtopic): Boolean = s match {
		case Subtopic(id, contents, hint) => {
			DB.withConnection {
				implicit session => {
					SQL(
						"""
						select
							count(*)
						from
							subtopics s
						where
							lower(s.contents) = lower({contents})
						""").on("contents" -> s.contents).as(scalar[Long].single) > 0
				}
			}
		}
		case _ => false
	}

	def delete(s: Subtopic): Boolean = s match {
		case Subtopic(id, contents, hint) => {
			DB.withConnection {
				implicit session => {
					SQL(
						"""
						delete from
							subtopics
						where
							id = {id}
						""").on("id" -> s.id).execute()
				}
			}
		}
		case _ => false
	}

	def getAll: List[Subtopic] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						*
					from
						subtopics
					""").as(parser*)
			}
		}
	}

	def matrix: List[List[String]] = {
		getAll map {
			case Subtopic(id, contents, hint) => {
				List(id.toString, contents, hint)
			}
		}
	}

	def getById(sid: Long): Option[Subtopic] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						*
					from
						subtopics s
					where
						s.id = {sid}
					""").on("sid" -> sid).as(parser*).headOption
			}
		}
	}

	def getByStepId(ssid: Long): List[Subtopic] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						s.id, s.contents, s.hint
					from
						subtopics s, subtopics_a sa
					where
						sa.subtopic_id = s.id
					and
						sa.step_id = {ssid}
					""").on("ssid" -> ssid).as(parser*)
			}
		}
	}

	def gen(id: Long = 0L, contents: String = "", hint: String = ""): Subtopic = {
		new Subtopic(id, contents, hint)
	}

	/* Formats the model for table presentation */
	override def toTable: List[List[String]] = {
		val colNames = List("ID", "Contents", "Hint")
		val subtopics = getAll
		val colVals = for(subtopic <- subtopics) yield List(subtopic.id.toString, subtopic.contents, subtopic.hint)
		colNames +: colVals // append the column names as the first row of the matrix
	}

}