package models.problems

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel

case class SolutionStep(
	id: Long,
	contents: String,
	picture: String,
	problemId: Long,
	position: Int,
	subtopics: List[Subtopic])

object Solution extends AnormModel {

	type T = SolutionStep

	val tableStatements = List(
		"create table if not exists solution_steps (id bigserial primary key, contents text, picture varchar, subtopic_id bigint, problem_id bigint, position int);",
		"create index solution_steps_i on solution_steps (subtopic_id, problem_id);")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("problem") ~ int("position") map {
		case id ~ contents ~ picture ~ problemId ~ position => SolutionStep(id, contents, picture, problemId, position, List())
	}

	val columns = List("id", "contents", "picture", "problem_id", "position")

	def create(ss: SolutionStep): Option[Long] = ss match {
		case SolutionStep(id, contents, picture, problemId, position, _) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						insert into solution_steps
							(contents, picture, subtopic_id, problem_id, position)
						values
							('${formatString(contents)}', '$picture', $problemId, $position)
						""").executeInsert()
				}
			}
		}
		case _ => None
	}

	def delete(ss: SolutionStep): Boolean = ss match {
		case SolutionStep(id, contents, picture, problemId, position, _) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						delete from
							solution_steps ss
						where
							ss.id = $id
						""").execute()
				}
			}
		}
		case _ => false
	}

	/* Retrieves solution steps in position by problem id */
	def getByProblemId(pid: Long): List[SolutionStep] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						(ss.id, ss.contents, ss.picture, ss.problem_id, ss.position)
					from
						solution_steps ss, subtopics s
					where
						ss.problem_id = $pid
					""").as(parser*).sortBy(x => x.position)
			}
		}
	}
}
