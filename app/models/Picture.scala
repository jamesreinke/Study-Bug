package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json._


case class Picture(
	id: Long,
	name: String,
	path: String)

object Picture extends JNorm[Picture] {

	val table = "pictures"

	val aTable = "pictures_a"

	val statements = List(
		"create table if not exists pictures (id bigserial primary key, name varchar, path varchar);"
		)

	val parser = long("id") ~ str("name") ~ str("path") map {
		case id ~ name ~ path => Picture(id, name, path)
	}

	def toJson(p: Picture): JsObject = {
		Json.obj(
			"id" -> p.id,
			"name" -> p.name,
			"path" -> p.path
			)
	}

	def create(p: Picture): Option[Long] = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					insert into pictures
						(name, path)
					values
						({name}, {path})
					""").on("name" -> p.name, "path" -> p.path).executeInsert()
			}
		}
	}

	def update(p: Picture): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					"""
					update
						pictures p
					set
						name = {name}, path = {path}
					where
						id = {id}
					""").on("name" -> p.name, "path" -> p.path, "id" -> p.id).executeUpdate()
			}
		}
	}
}