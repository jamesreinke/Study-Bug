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
	subtopic: Long,
	problem: Long,
	position: Int)

object Solution extends AnormModel {

	type T = SolutionStep

	val tableStatements = List(
		"create table if not exists solution_steps (id bigserial primary key, contents text, picture varchar, subtopic bigint, problem bigint, position int);",
		"create index solution_steps_i on solution_steps (subtopic, problem);")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("subtopic") ~ long("problem") ~ int("position") map {
		case id ~ contents ~ picture ~ subtopic ~ problem ~ position => SolutionStep(id, contents, picture, subtopic, problem, position)
	}

	val columns = List("id", "contents", "picture", "subtopic", "problem", "position")

	def create(ss: SolutionStep): Option[Long] = ss match {
		case SolutionStep(id, contents, picture, subtopic, problem, position) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						insert into solution_steps
							(contents, picture, subtopic, problem, position)
						values
							('${formatString(contents)}', '$picture', $subtopic, $problem, $position)
						""").executeInsert()
				}
			}
		}
		case _ => None
	}

	def delete(ss: SolutionStep): Boolean = ss match {
		case SolutionStep(id, contents, picture, subtopic, problem, position) => {
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
						*
					from
						solution_steps ss
					where
						ss.problem = $pid
					""").as(parser*).sortBy(x => x.position)
			}
		}
	}
}
