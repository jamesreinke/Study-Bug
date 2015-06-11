package models.users

import anorm._
import anorm.SqlParser._

import play.api.db.DB
import play.api.Play.current

import scala.collection.immutable._

import com.github.t3hnar.bcrypt._

import models.AnormModel

case class Login(
	email: String,
	password: String)

case class Register(
	email: String,
	passwords: (String, String))

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

	def create(user: User): Option[Long] = user match {
		case User(id, email, password, admin) => {
			if( !exists(user) ) {
				DB.withConnection {
					implicit session => {
						SQL(
							s"""
							insert into users
								(id, email, password, admin)
							values
								($id, '$email', '${password.bcrypt}', $admin)
							""").executeInsert()
					}
				}
			}
			else None
		}
		case _ => None
	}

	def delete(user: User): Boolean = user match {
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

	def exists(user: User): Boolean = user match {
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
							lower(u.email) = lower($email)
						""").as(scalar[Long].single) > 0
				}
			}
		}
		case _ => false
	}

	def login(user: Login): Option[User] = user match {
		case Login(email, password) => {
			DB.withConnection {
				implicit session => {
					SQL(
						s"""
						select
							(id, email, password, admin)
						from
							users u
						where
							lower(u.email) = lower($email)
						and
							u.password = ${password.bcrypt}
						""").as(parser*).headOption
				}
			}
		}
		case _ => None
	}
}