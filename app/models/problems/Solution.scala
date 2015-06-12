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
		"create table if not exists solution_steps (id bigserial primary key, contents text, picture varchar, problem_id bigint, position int);",
		"create index solution_steps_i on solution_steps (problem_id);")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("problem_id") ~ int("position") map {
		case id ~ contents ~ picture ~ problemId ~ position => SolutionStep(id, contents, picture, problemId, position, List())
	}

	val columns = List("id", "contents", "picture", "problem_id", "position")

	def create(ss: SolutionStep): Option[Long] = ss match {
		case SolutionStep(id, contents, picture, problemId, position, subtopics) => {
			val ssid = DB.withConnection {
				implicit session => {
					SQL(
						s"""
						insert into solution_steps
							(contents, picture, problem_id, position)
						values
							('${formatString(contents)}', '$picture', $problemId, $position)
						""").executeInsert()
				}
			}
			// assign the subtopics to the solution step
			for(subtopic <- subtopics) {
				Subtopic.assign(subtopic.id, ssid.getOrElse(0))
			}
			ssid // return value
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
						*
					from
						solution_steps ss
					where
						ss.problem_id = $pid
					""").as(parser*).map(x => {
							val subtopics: List[Subtopic] = Subtopic.getByStepId(x.id)
							new SolutionStep(
							x.id, 		x.contents, 
							x.picture, 	x.problemId, 
							x.position, subtopics)
						}).sortBy(x => x.position)  // we 'tack' on the subtopics after the query is complete
			}
		}
	}
}
