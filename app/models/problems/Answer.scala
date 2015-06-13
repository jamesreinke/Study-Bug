package models.problems

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel

case class Answer(
	id: Long,
	contents: String,
	picture: String,
	problemId: Long,
	correct: Boolean
	)

object Answer extends AnormModel {

	type T = Answer

	val tableStatements = List(
		"create table if not exists answers (id bigserial primary key, contents text, picture varchar, problem_id bigint, correct boolean);",
		"create index answers_i on answers (problem_id);")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("problem_id") ~ bool("correct") map {
		case id ~ contents ~ picture ~ problemId ~ bool => Answer(id, contents, picture, problemId, bool)
	}

	def create(a: Answer): Option[Long] = a match {
		case Answer(id, contents, picture, problemId, correct) => {
				DB.withConnection {
					implicit session => {
						SQL(
							s"""
							insert into answers
								(contents, picture, problem_id, correct)
							values
								('${formatString(contents)}', '$picture', $problemId, $correct)
							""").executeInsert()
					}
				}
		}
		case _ => None
	}

	def delete(a: Answer): Boolean = a match {
		case Answer(id, contents, picture, problemId, correct) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						delete from
							answers a
						where
							a.id = $id
						""").execute()
				}
			}
		}
		case _ => false
	}

	def getByProblemId(pid: Long): List[Answer] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select 
						*
					from
						answers a
					where
						a.problem_id = $pid
					""").as(parser*)
			}
		}
	}
}