package models

import play.api.Play.current

import anorm._

import play.api.db.DB

import scala.util.Random

import play.api.libs.json._


abstract class JNorm [T]{

	/* Table name identifier */
	val table: String

	/* SQL create statements */
	val statements: List[String]

	/* T Parser */
	val parser: RowParser[T]

	/* Maps the case class to a Json object */
	def toJson(s: T): JsObject

	/* Inserts an item into the table */
	def create(item: T): Option[Long]

	/* Updates object of type T */
	def update(s: T): Int

	/* Removes the database item */
	def delete(id: Long): Int = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					delete from 
						$table
					where
						id = {id}
					""").on("id" -> id).executeUpdate()
			}
		}
	}

	def get(id: Long): Option[T] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						$table
					where
						id = {id}
					""").on("id" -> id).as(parser*).headOption
			}
		}
	}

	def getAll: List[T] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						$table
					""").as(parser*)
			}
		}
	}

	/* Initialize the database */
	def init: Unit = {
		println("Initializing table: " + table)
		DB.withConnection {
			implicit session => {
				val orders = dropQueries(statements) ++ statements
				if(current.configuration.getString("dbReset").getOrElse("false") == "true"){
					for(order <- orders) {
						SQL(order).execute()
					}
				}
			}
		}
	}

	/* Helper methods */

	/* Randomizes the order of a list of rows */
	private def shuffle(list: List[T]): List[T] = {
		scala.util.Random.shuffle(list)
	}

	/* Extracts query information, i.e. name and table/index */
	private def identify(statement: String): Option[(String, String)] = {
		val Regex = """create\s+(table|index)\s+if\s+not\s+exists\s+([^\s]+)[(\s+)(\()].*""".r
			statement match {
				case Regex(sType, key) => Some((sType, key))
				case _ => None
			}
	}

	/* Generate table drop queries */
	private def dropQueries(statements: List[String], result: List[String] = List()): List[String] = {
		statements match {
			case List() => result
			case _ => identify(statements.head) match {
				case Some((sType, key)) => dropQueries(statements.tail, result :+ ("drop " + sType + " if exists " + key + ";"))
				case None => dropQueries(statements.tail, result)
			}
		}
	}

}