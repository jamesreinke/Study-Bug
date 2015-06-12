package models.problems


import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import models.AnormModel

case class Topic(
	id: Long,
	contents: String,
	parent: Long)

object Topic extends AnormModel {

	type T = Topic

	val tableStatements = List(
		"create table if not exists topics (id bigserial primary key, contents text, parent bigint);",
		"create index topics_i on topics using gin(to_tsvector('english', contents));",
		"create table if not exists topics_a (id bigserial primary key, topic bigint, problem bigint);",
		"create index topics_ai on topics_a (topic, problem);")


	val parser = long("id") ~ str("contents") ~ long("parent") map {
		case id ~ contents ~ parent => Topic(id, contents, parent)
	}

	val columns = List("id", "contents", "parent")

	def create(t: Topic): Option[Long] = t match {
		case Topic(id, contents, parent) => {
			if( !exists(t) ) {
				DB.withConnection {
					implicit session => {
						SQL(
						s"""
						insert into topics
							(contents, parent)
						values
							('${formatString(contents)}', $parent)
						""").executeInsert()
					}
				}
			}
			else None
		}
		case _ => None
	}

	def exists(t: Topic): Boolean = t match {
		case Topic(id, contents, parent) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						select 
							count(*)
						from
							topics t
						where
							lower(t.contents) = lower('${formatString(contents)}')
						""").as(scalar[Long].single) > 0
				}
			}
		}
	}

	def delete(t: Topic): Boolean = t match {
		case Topic(id, contents, parent) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						delete from
							topics t
						where
							t.id = $id
						""").execute()
				}
			}
		}
		case _ => false
	}

	def getAll: List[Topic] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						topics t
					""").as(parser*)
			}
		}
	}

	def matrix: List[List[String]] = {
		getAll map {
			case Topic(id, contents, parent) => {
				List(id.toString, contents, parent.toString)
			}
		}
	}

	/* Returns the parent topic if there is one */
	private def getParent(t: Topic): Option[Topic] = t match {
		case Topic(id, contents, parent) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						select
							*
						from
							topics t
						where
							t.id = $parent
						""").as(parser*).headOption
				}
			}
		}
		case _ => None
	}

	/* Returns a list in order from leaf to root of all parents of a given topic */
	def getParents(t: Topic, list: List[Topic] = List()): List[Topic] = {
		getParent(t) match {
			case Some(Topic(id, contents, parent)) => {
				val t = Topic(id, contents, parent)
				getParents(t, list :+ t)
			}
			case _ => list
		}
	}

	def getChildren(t: Topic, list: List[Topic] = List()): List[Topic] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						topics t
					where
						t.parent = ${t.id}
					""").as(parser*)
			}
		}
	}

	/* Returns all topics assigned to a problem id */
	def getByProblemId(pid: Long): List[Topic] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						t.id, t.contents, t.parent
					from
						topics t, topic_assignments ta
					where
						ta.problem = $pid
					and
						t.id = ta.topic
					""").as(parser*)
			}
		}
	}
}