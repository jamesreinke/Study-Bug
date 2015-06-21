package models.problems


import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel
import models.JNorm
import play.api.libs.json._

case class Subtopic(
	id: Long,
	contents: String,
	hint: String)

object Subtopic extends JNorm[Subtopic] {

	val table = "subtopics"

	val aTable = "subtopics_a"

	val statements = List(
		"create table if not exists subtopics (id bigserial primary key, contents text, hint text);",
		"create index subtopics_i on subtopics using gin(to_tsvector('english', contents));",
		"create table if not exists subtopics_a (id bigserial primary key, subtopic_id bigint, step_id bigint);",
		"create index subtopics_ai on subtopics_a (subtopic_id, step_id);")

	val parser = long("id") ~ str("contents") ~ str("hint") map {
		case id ~ contents ~ hint => Subtopic(id, contents, hint)
	}

	def toJson(s: Subtopic): JsObject = {
		Json.obj(
			"id" -> s.id,
			"contents" -> s.contents,
			"hint" -> s.hint
			)
	}

	def create(s: Subtopic): Option[Long] = {
		if( !exists(s) ) {
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

	/* Formats the model for table presentation */
	def toTable: List[List[String]] = {
		val colNames = List("ID", "Contents", "Hint")
		val subtopics = getAll
		val colVals = for(subtopic <- subtopics) yield List(subtopic.id.toString, subtopic.contents, subtopic.hint)
		colNames +: colVals // append the column names as the first row of the matrix
	}

}