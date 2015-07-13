package models.problems

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel
import models.JNorm
import play.api.libs.json._

/* One of many steps in a solution */
case class Step(
	id: Long,
	contents: String,
	subtopic: Long,
	picture: Long,
	pid: Long,
	stepNum: Int
	)

object Solution extends JNorm[Step] {

	val table = "solutions"

	val aTable = "solutions_a"

	val statements = List(
		"create table if not exists solutions (id bigserial primary key, contents text, subtopic bigint, picture bigint, pid bigint, step_num int);",
		"create index solutions_i on solutions (pid, subtopic);")

	val parser = long("id") ~ str("contents") ~ long("subtopic") ~ long("picture") ~ long("pid") ~ int("step_num") map {
		case id ~ contents ~ subtopic ~ picture ~ pid ~ stepNum => Step(id, contents, subtopic, picture, pid, stepNum)
	}

	def toJson(s: Step): JsObject = {
		Json.obj(
			"id" -> s.id,
			"contents" -> s.contents,
			"subtopic" -> s.subtopic,
			"picture" -> s.picture,
			"pid" -> s.pid,
			"stepnum" -> s.stepNum
			)
	}

	def create(s: Step): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into solutions
						(contents, subtopic, picture, pid, step_num)
					values
						({contents}, {subtopic}, {picture}, {pid}, {stepNum})
					""").on("contents" -> s.contents, "subtopic" -> s.subtopic, "picture" -> s.picture, "pid" -> s.pid, "stepNum" -> s.stepNum)
						.executeInsert()
			}
		}
	}

	def update(s: Step): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update
						solutions
					set
						contents = {contents}, subtopic = {subtopic}, picture = {picture}, pid = {pid}, step_num = {stepNum}
					where
						id = {id}
					""").on(
					"id" -> s.id,
					"contents" -> s.contents,
					"subtopic" -> s.subtopic,
					"picture" -> s.picture,
					"pid" -> s.pid,
					"stepNum" -> s.stepNum).executeUpdate()
			}
		}
	}
	/* Returns the entire solution in order by step number */
	def getByProblemId(pid: Long): List[Step] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						*
					from
						solutions s
					where
						s.pid = {pid}
					order by
						s.step_num
					""").on("pid" -> pid).as(parser*)
			}
		}
	}
}