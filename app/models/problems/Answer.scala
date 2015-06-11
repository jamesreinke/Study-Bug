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
	problem: Long,
	correct: Boolean
	)

object Answer extends AnormModel {

	type T = Answer

	val tableStatements = List(
		"create table if not exists answers (id bigserial primary key, contents text, picture varchar, problem bigint, correct boolean);",
		"create index answers_i on answers (problem);")

	val columns = List("id", "contents", "picture", "problem", "correct")

	val parser = long("id") ~ str("contents") ~ str("picture") ~ long("problem") ~ bool("correct") map {
		case id ~ contents ~ picture ~ problem ~ bool => Answer(id, contents, picture, problem, bool)
	}

	def create(answer: Answer): Option[Long] = answer match {
		case Answer(id, contents, picture, problem, correct) => {
				DB.withConnection {
					implicit session => {
						SQL(
							s"""
							insert into answers
								(id, contents, picture, problem, correct)
							values
								($id, ${formatString(contents)}, ${formatString(picture)}, $problem, $correct)
							""").executeInsert()
					}
				}
		}
		case _ => None
	}

	def delete(item: Answer): Boolean = item match {
		case Answer(id, contents, picture, problem, correct) => {
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
						(id, contents, picture, problem, correct)
					from
						answers a
					where
						a.problem = $pid
					""").as(parser*)
			}
		}
	}
}