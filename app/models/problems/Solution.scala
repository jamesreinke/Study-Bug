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
	order: Int)

object Solution extends AnormModel {

	type T = SolutionStep

	val tableStatements = List(
		"create table if not exists solution_steps (id bigserial primary key, contents text, picture varchar, subtopic bigint, problem bigint, order int);",
		"create solution_steps_i on solution_steps (subtopic, problem);")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("subtopic") ~ long("problem") ~ int("order") map {
		case id ~ contents ~ picture ~ subtopic ~ problem ~ order => SolutionStep(id, contents, picture, subtopic, problem, order)
	}

	val columns = List("id", "contents", "picture", "subtopic", "problem", "order")

	def create(ss: SolutionStep): Option[Long] = ss match {
		case SolutionStep(id, contents, picture, subtopic, problem, order) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						insert into solution_steps
							(id, contents, picture, subtopic, problem, order)
						values
							($id, ${formatString(contents)}, $picture, $subtopic, $problem, $order)
						""").executeInsert()
				}
			}
		}
		case _ => None
	}

	def delete(ss: SolutionStep): Boolean = ss match {
		case SolutionStep(id, contents, picture, subtopic, problem, order) => {
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

	/* Retrieves solution steps in order by problem id */
	def getByProblemId(pid: Long): List[SolutionStep] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						(id, contents, picture, subtopic, problem, order)
					from
						solution_steps ss
					where
						ss.problem = $pid
					""").as(parser*).sortBy(x => x.order)
			}
		}
	}
}
