package models.problems

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel

case class Problem(
	id: Long,
	contents: String,
	pictures: List[Picture],
	answers: List[Answer],
	solution: List[SolutionStep],
	topic: Topic)

case class Picture(
	id: Long,
	problem_id: Long,
	picture: String,
	order: Int)


object Problem extends AnormModel {

	type T = Problem

	val tableStatements = List(
		"create table if not exists problems (id bigserial primary key, contents text, topic_id bigint);",
		"create index problems_i on problems (to_tsvector('english', contents));",
		"create table if not exists problems_p (id bigserial primary key, problem_id bigint, picture varchar, order int);",
		"create index problems_pi on problems_p (problem_id);")

	val parser = long("id") ~ str("contents") ~ long("topic_id") ~ Topic.parser map {
		case id ~ contents ~ topicId ~ topic => Problem(id, contents, List(), List(), List(), topic)
	}

	val picParser = long("id") ~ long("problem_id") ~ str("picture") ~ int("order") map {
		case id ~ problemId ~ picture ~ order => Picture(id, problemId, picture, order)
	}

	def create(p: Problem): Option[Long] = {
		DB.withConnection {
			implicit session => {
				/* store problem */
				val opid = SQL(
					s"""
					insert into problems
						(contents, topic_id)
					values
						('${formatString(p.contents)}', ${p.topic.id})
					""").executeInsert()
				val pid = opid.getOrElse(0L)
				/* store problem pictures */
				for((pic,i) <- p.pictures.view.zipWithIndex) {
					SQL(
						s"""
						insert into problems_p
							(problem_id, picture, order)
						values
							(${pid}, '${formatString(pic.picture)}', $i)
						""").executeInsert()
				}
				/* store answer choices */
				for(answer <- p.answers) {
					Answer.create(new Answer(0, answer.contents, answer.picture, pid, answer.correct))
				}
				/* store solution steps */
				for (step <- p.solution) {
					Solution.create(new SolutionStep(0, step.contents, step.picture, pid, step.position, step.subtopics))
				}
				opid // return value
			}
		}
	}

	def delete(p: Problem): Boolean = {
		DB.withConnection {
			implicit session => {
				var ret = SQL(
					s"""
					delete from
						problems p
					where
						p.id = ${p.id}
					""").execute()
				for(ss <- p.solution) {
					ret = ret && Solution.delete(ss)
				}
				for(a <- p.answers) {
					ret = ret && Answer.delete(a)
				}
				ret
			}
		}
	}

	def getPicturesById(pid: Long): List[Picture] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						porblems_p pp
					where
						pp.id = $pid
					""").as(picParser*)
			}
		}
	}

	def getAll: List[Problem] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						p.id, p.contents, t.id, t.contents, t.parent
					from
						problems p, topics t
					""").as(parser*).map(x => {
						new Problem(
							x.id, 
							x.contents,
							getPicturesById(x.id),
							Answer.getByProblemId(x.id),
							Solution.getByProblemId(x.id),
							x.topic)
					})
			}
		}
	}
}