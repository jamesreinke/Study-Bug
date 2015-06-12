package models
import play.api.Play.current
import anorm._
import play.api.db.DB
import scala.util.Random


abstract class AnormCase

abstract class AnormModel {

	type T

	/* Postgres table generation statements */
	val tableStatements: List[String]

	/* Parser for parsing the case class */
	val parser: RowParser[T]

	/* Column name access */
	val columns: List[String]

	/* Insert an item into the database, returning the auto incremented id */
	def create(item: T): Option[Long]

	/* Removes the database item */
	def delete(item: T): Boolean

	
	/* 

	Helper functions 

	*/

	/* Toggle whether yes or no to recreate database tables */
	val reset: Boolean = current.configuration.getString("dbReset").getOrElse("false") == "true"

	/* Postgres string formatting for database values */
	def formatString(s: String): String = {
		s.replaceAll("'", "''")
	}

	/* Extracts query information, i.e. name and table/index */
	private def identify(statement: String): Option[(String, String)] = {
		val Regex = """create\s+(table|index)\s+if\s+not\s+exists\s+([^\s]+)[(\s+)(\()].*""".r
			statement match {
				case Regex(sType, key) => Some((sType, key))
				case _ => None
			}
	}

	/*
		Write a regex to identify column names
	*/

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

	/* Initialize the database */
	def init: Unit = {
		DB.withConnection {
			implicit session => {
				val statements = dropQueries(tableStatements) ++ tableStatements
				if(reset){
					for(statement <- statements) {
						SQL(statement).execute()
					}
				}
			}
		}
	}

	/* Factory method for randomizing the order of a list of rows */
	def shuffle(list: List[T]): List[T] = {
		util.Random.shuffle(list)
	}
}