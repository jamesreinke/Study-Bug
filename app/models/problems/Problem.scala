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
	difficulty: Int
	)


object Problem extends JNorm[Problem] {

	val table = "problems"

	val statements = List(
		"create table if not exists problems (id bigserial primary key, contents text, topic text);",
		"create index problems_i on problems using gin(to_tsvector('english', contents));",
		"create table if not exists problems_p (id bigserial primary key, pid bigint, pic_id bigint);",
		"create index problems_a_i on problems_p (pid, pic_id);",
		"create table if not exists problems_cp (id bigserial primary key, pid bigint, pic_id bigint);",
		"create index problems_cpi on problems_cp (pid, pic_id);")

	val parser = long("id") ~ str("contents") ~ str("topic") ~ int("difficulty") map {
		case id ~ contents ~ topic ~ difficulty => Problem(id, contents, topic, difficulty)
	}

	def toJson(p: Problem): JsObject = {
		Json.obj(
			"id" -> p.id,
			"contents" -> p.contents,
			"topic" -> p.topic,
			"difficulty" -> p.difficulty
			)
	}

	def assignContentPicture(pid: Long, picId: Long): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into problems_cp
						(pid, pic_id)
					values
						({pid}, {picId})
					""").on("pid" -> pid, "picId" -> picId).executeInsert()
			}
		}
	}

	/* Retrieves all content pictures */
	def getContentPictures(pid: Long): List[models.Picture] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						p.id, p.name, p.path
					from
						pictures p, problems_cp cp
					where
						p.id = cp.pic_id
					and
						cp.pid = {pid}
					""").on("pid" -> pid).as(models.Picture.parser*)
			}
		}
	}

	def clearContentPictures(pid: Long): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					delete from
						problems_cp cp
					where
						cp.id = {pid}
					""").on("pid" -> pid).executeUpdate()
			}
		}
	}

	def create(p: Problem): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into problems
						(contents, topic, difficulty)
					values
						({contents}, {topic}, {difficulty})
					""").on("contents" -> p.contents, "topic" -> p.topic, "difficulty" -> p.difficulty).executeInsert()
			}
		}
	}

	def update(p: Problem): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update 
						problems
					set
						contents = {contents}, topic = {topic}, difficulty = {difficulty}
					where
						id = {pid}
					""").on("contents" -> p.contents, "topic" -> p.topic, "pid" -> p.id, "difficulty" -> p.difficulty).executeUpdate()
			}
		}
	}

	def getByTopic(t: String): List[Problem] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						*
					from
						problems
					where
						topic = {topic}
					""").on("topic" -> t).as(parser*)
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
						p.id, p.name, p.path
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