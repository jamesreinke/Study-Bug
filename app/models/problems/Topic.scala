package models.problems


import anorm._

import anorm.SqlParser._

import play.api.db.DB

import play.api.Play.current

import models.AnormModel

import models.JNorm

import play.api.libs.json._

case class Topic(
	id: Long,
	contents: String,
	parent: Long)

object Topic extends JNorm[Topic] {

	val table = "topics"

	val aTable = "DOES NOT EXIST"

	val statements = List(
		"create table if not exists topics (id bigserial primary key, contents text, parent bigint);",
		"create index topics_i on topics using gin(to_tsvector('english', contents));")

	val parser = long("id") ~ str("contents") ~ long("parent") map {
		case id ~ contents ~ parent => Topic(id, contents, parent)
	}

	def toJson(t: Topic): JsObject = {
		Json.obj(
			"id" -> t.id,
			"contents" -> t.contents,
			"parent" -> t.parent)
	}

	def create(t: Topic): Option[Long] = {
		if( !exists(t) ) {
			DB.withConnection { 
				implicit session => {
					SQL(
						"""
						insert into topics
							(contents, parent)
						values
							({contents}, {parent})
						""").on("contents" -> t.contents, "parent" -> t.parent).executeInsert()
				}
			}
		}
		else None
	}

	def update(t: Topic): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update 
						topics t
					set
						contents = {contents}, parent = {parent}
					where
						t.id = {tid}
					""").on("tid" -> t.id, "contents" -> t.contents, "parent" -> t.parent).executeUpdate()
			}
		}
	}

	/* Checks whether a topic already exists by contents */
	def exists(t: Topic): Boolean = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					select
						count(*)
					from
						topics t
					where
						lower(t.contents) = lower({contents})
					""").on("contents" -> t.contents).as(scalar[Long].single) > 0
			}
		}
	}

	/* Formats the model for table presentation */
	def toTable: List[List[String]] = {
		val colNames = List("ID", "Contents", "Parent")
		val topics = getAll
		val colVals = for(topic <- topics) yield List(topic.id.toString, topic.contents, topic.parent.toString)
		colNames +: colVals // append the column names as the first row of the matrix
	}

}