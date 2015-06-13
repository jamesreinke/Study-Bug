package models.users

import anorm._
import anorm.SqlParser._

import play.api.db.DB
import play.api.Play.current

import scala.collection.immutable._

import com.github.t3hnar.bcrypt._

import models.AnormModel

case class User(
	id: Long,
	email: String,
	password: String,
	admin: Boolean)

object Authentication extends AnormModel {

	type T = User

	val tableStatements = List(
		"create table if not exists users (id bigserial primary key, email varchar, password varchar, admin bool);")

	val columns = List("id", "email", "password", "admin")

	val parser = long("id") ~ str("email") ~ str("password") ~ bool("admin") map {
		case id ~ email ~ password ~ admin => User(id, email, password, admin)
	}

	def create(u: User): Option[Long] = u match {
		case User(id, email, password, admin) => {
			if( !exists(u) ) {
				DB.withConnection {
					implicit session => {
						SQL(
							s"""
							insert into users
								(email, password, admin)
							values
								('$email', '${password.bcrypt}', $admin)
							""").executeInsert()
					}
				}
			}
			else None
		}
		case _ => None
	}

	def delete(u: User): Boolean = u match {
		case User(id, email, password, admin) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						delete from 
							users u
						where
							u.id = $id
						""").execute()
				}
			}
		}
		case _ => false
	}

	def exists(u: User): Boolean = u match {
		case User(id, email, password, admin) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						select 
							count(*)
						from
							users u
						where
							lower(u.email) = lower('$email')
						""").as(scalar[Long].single) > 0
				}
			}
		}
		case _ => false
	}

	def login(u: User): Option[User] = u match {
		case User(_, email, password, _) => {
			getByEmail(email) match {
				case Some(User(id, userEmail, userPassword, admin)) => {
					password.isBcrypted(userPassword) match {
						case true => Some(new User(id, userEmail, userPassword, admin))
						case false => None
					}
				}
				case _ => None
			}
		}
		case _ => None
	}

	private def getByEmail(e: String): Option[User] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						users u
					where
						lower(u.email) = lower('$e')
					""").as(parser*).headOption
			}
		}
	}

	def getAll: List[User] = {
		DB.withConnection {
			implicit session => {
				SQL(
					s"""
					select
						*
					from
						users
					""").as(parser*)
			}
		}
	}
}