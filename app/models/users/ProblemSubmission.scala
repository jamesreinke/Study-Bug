package models.users

import anorm._
import anorm.SqlParser._

import play.api.db.DB
import play.api.Play.current

import scala.collection.immutable._

import com.github.t3hnar.bcrypt._

import models.AnormModel

import play.api.libs.json._

case class Submission(
	id: Long,
	user: Long,
	answerID: Long)

object ProblemSubmission extends models.JNorm[Submission] {

	val table = "submissions"

	val statements = List(
		"create table if not exists submissions (id bigserial primary key, user_id bigint, answer_id bigint);",
		"create index submissions_i on submissions (user_id, answer_id);"
		)

	val parser = long("id") ~ long("user_id") ~ long ("answer_id") map {
		case id ~ user ~ answerID => Submission(id, user, answerID)
	}

	def toJson(s: Submission) = {
		Json.obj(
			"id" -> s.id,
			"user" -> s.user,
			"answerid" -> s.answerID
			)
	}


	def create(s: Submission): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into submissions
						(user_id, answer_id)
					values
						({user}, {answerID})
					""").on("user" -> s.user, "answerID" -> s.answerID).executeInsert()
			}
		}
	}

	def update(s: Submission): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update
						submissions
					set
						user_id = {user}, answer_id = {answerID}
					where
						id = {id}
					""").on("user" -> s.user, "answerID" -> s.answerID, "id" -> s.id).executeUpdate()
			}
		}
	}

}