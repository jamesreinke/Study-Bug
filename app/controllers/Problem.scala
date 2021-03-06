package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import Node._

import views.html.defaultpages._

import Authentication.{auth, admin}

import views.html.components.problem._

import play.api.libs.json._

import Play.current

object Amazon {

	import jp.co.bizreach.s3scala.S3
	import awscala.s3._
	import awscala.Region

	implicit val region = Region.NorthernCalifornia
	var key = ""

	implicit val s3 = S3(
		accessKeyId = sys.env("AWS_ACCESS_KEY"), 
		secretAccessKey = sys.env("AWS_SECRET_ACCESS_KEY"))

	var bucket: Bucket = null
	val buckets: Seq[Bucket] = s3.buckets
	
	buckets foreach { x => if(x.name == sys.env("S3_BUCKET_NAME")) bucket = x }

}


object Problem extends Controller {

	import Amazon.{region, s3}

	import models.problems.Problem
	import models.Picture
	import java.io.File

	def database = Action {
		implicit request => Ok(views.html.pages.temp.core(todo.render()))
	}

	/* GET - Loads subtopic database page */
	def getPage = Action {
		implicit request => {
			Authentication.admin(request) match {
				case true => {
					Ok(views.html.pages.temp.core(
						content = core(),
						exStyles = styles(),
						exJavascripts = _javascripts()))
				}
				case false => {
					Redirect("")
				}
			}
		}
	}


	/* POST - upload problem related pictures 
		We use a path variable to accomidate the dropbox module functionality
	*/
	def pictures(id: Long) = Action(parse.multipartFormData) {
		implicit request => {
			request.body.file("pic").map {
				pic => {
					val r = scala.util.Random
					val f = new java.io.File(r.nextInt.toString)
					pic.ref.moveTo(f)
					Amazon.bucket.put(pic.filename, f)
					f.delete
					val s3obj: Option[awscala.s3.S3Object] = Amazon.bucket.getObject(pic.filename)
					var path = ""
					s3obj foreach { x => path = x.publicUrl.toString}
					Picture.create(new Picture(0, pic.filename, path)) match {
						case Some(long) => {
							models.problems.Problem.assign(id, long) match {
								case Some(long) => Ok(Picture.toJson(new Picture(long, pic.filename, path)))
								case _ => BadRequest("Error while assigning picture to problem")
							}
						}
						case _ => BadRequest("Error while adding picture to the database")
					}
				}
				}.getOrElse {
					BadRequest("Could not upload file")
			}
		}
	}


	/*
		Content Pictures
	*/
	val iForm = Form(
		tuple(
			"pid" -> default(of[Long], 0L),
			"picture_id" -> default(of[Long], 0L)))
	def addContentPicture = Action {
		implicit request => {
			val (pid, picId) = iForm.bindFromRequest.get
			models.problems.Problem.assignContentPicture(pid, picId) match {
				case Some(long) => Ok("Success!")
				case _ => BadRequest("Error while generating content picture for problem: " + pid)
			}
		}
	}
	def getContentPictures = Action {
		implicit request => {
			val (id, contents, topic, difficulty) = pForm.bindFromRequest.get
			val pics = models.problems.Problem.getContentPictures(id) // list of Pictures
			val jsonArray = Json.obj("pictures" -> pics.map(x => models.Picture.toJson(x)))
			Ok(jsonArray)
		}
	}

	def clearContentPictures = Action {
		implicit request => {
			val (pid, picId) = iForm.bindFromRequest.get
			val num = models.problems.Problem.clearContentPictures(pid)
			Ok("Deleted " + num + " contenet pictures for pid: " + pid)
		}
	}

	val pictureDeleteForm = Form(
		tuple(
			"pid" -> default(of[Long], 0L),
			"pic" -> default(of[Long],0L)))

	def picDelete =  Action {
		implicit request => {
			val (pid, pic) = pictureDeleteForm.bindFromRequest.get
			Picture.get(pic) match {
				case Some(Picture(id, name, path)) => {
					models.problems.Problem.unassign(pid, pic)
					Picture.delete(id)
					Amazon.bucket.delete(name)
					Ok("Sucessfully deleted picture name: " + name)
				}
				case _ => BadRequest("Error while retrieving filepath from picture id")
			}	
		}
	}

	val pForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"contents" -> default(text, ""),
			"topic" -> default(text, ""),
			"difficulty" -> default(of[Int], 1)))

	def delete = Action {
		implicit request => {
			val (id, contents, topic, difficulty) = pForm.bindFromRequest.get
			models.problems.Problem.delete(id) > 0 match {
				case true => Ok("Deleted problem ID: " + id)
				case false => BadRequest("Unable to delete from ID: " + id)
			}
		}
	}

	def get = Action {
		implicit request => {
			val (id, contents, topic, difficulty) = pForm.bindFromRequest.get
			models.problems.Problem.get(id) match {
				case Some(problem) => Ok(problemsToJson(List(problem))) // transform problem to a list and grab aux information to form the problem statement
				case _ => BadRequest("Error retrieving problem ID: " + id)
			}
		}
	}

	def create = Action {
		implicit request => {
			val (id, contents, topic, difficulty) = pForm.bindFromRequest.get
			models.problems.Problem.create(new Problem(id, contents, topic, difficulty)) match {
				case Some(long) => {
					Ok(models.problems.Problem.toJson(new Problem(long, contents, topic, difficulty)))
				}
				case _ => BadRequest("Error while inserting new problem into database")
			}
		}
	}

	def getPictures = Action {
		implicit request => {
			val (id, contents, topic, difficulty) = pForm.bindFromRequest.get
			val pics = models.problems.Problem.getAllPictures(id) // list of Pictures
			val jsonArray = Json.obj("pictures" -> pics.map(x => models.Picture.toJson(x)))
			Ok(jsonArray)
		}
	}

	/* POST - Updates a subtopic */
	def update = Action {
		implicit request => {
			val (id, contents, topic, difficulty) = pForm.bindFromRequest.get
			models.problems.Problem.update(new Problem(id, contents, topic, difficulty)) match {
				case 1 => Ok("Success")
				case _ => BadRequest("Could not update problem ID: " + id)
			}
		}
	}
	val aForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"contents" -> default(text, ""),
			"picture" -> default(text, ""),
			"pid" -> default(of[Long], 0L),
			"correct" -> default(of[Int], 1),
			"closeness" -> default(of[Int], 0)))

	/* POST - Creates/Updates an answer for a given problem identified by ID */
	def postAnswer = Action {
		implicit request => {
			import models.problems.Answer
			val (id, contents, picture, pid, correctVal, closeness) = aForm.bindFromRequest.get
			val correct = correctVal == 1
			println(id, contents, picture, pid, correctVal, closeness)
			println("adding asnwer with id: " + id)
			id > 0 match {
				// create new answer
				case false => {
					Answer.create(new Answer(id, contents, picture, pid, correct, closeness)) match {
						case Some(long) => {
							Ok(long.toString)
						}
						case _ => BadRequest("Unable to create answer Contents: " + contents)
					}
				}
				// edit an existing answer
				case true => {
					Answer.update(new Answer(id, contents, picture, pid, correct, closeness)) > 0 match {
						case false => BadRequest("Unable to update answer ID: " + id)
						case true => Ok("Successfully updated answer ID: " + id)
					}
				}
			}
		}
	}
	def getAnswers = Action {
		implicit request => {
			val id = aForm.bindFromRequest.get._1
			models.problems.Answer.getByProblemId(id) match {
				case List() => BadRequest("No answer matching ID: " + id)
				case answers => {
					val jsonArray = Json.obj("answers" -> answers.map(x => models.problems.Answer.toJson(x)))
					Ok(jsonArray)
				}
			}
		}
	}

	val sForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"contents" -> default(text, ""),
			"subtopic" -> default(of[Long], 0L),
			"picture" -> default(text, ""),
			"pid" -> default(of[Long], 0L),
			"stepNum" -> default(of[Int], -1)))
	/* POST - Creates/Updates solution steps for a given probelm identified by ID */
	def postStep = Action {
		implicit request => {
			import models.problems.Solution
			import models.problems.Step
			val (id, contents, subtopic, picture, pid, stepNum) = sForm.bindFromRequest.get
			println("Solution values", id, contents, subtopic, picture, pid, stepNum)
			id > 0 match {
				case false => {
					Solution.create(new Step(id, contents, subtopic, picture, pid, stepNum)) match {
						case Some(long) => Ok(long.toString)
						case _ => BadRequest("Unable to create solution step Contents: " + contents)
					}
				}
				case true => {
					Solution.update(new Step(id, contents, subtopic, picture, pid, stepNum)) > 0 match {
						case false => BadRequest("Unable to update solution step ID: " + id)
						case true => Ok("successfully updated solution step ID: " + id)
					}
				}
			}
		}
	}
	/* Transforms a list of problems into a JSON Array */
	private def problemsToJson(probs: List[Problem]): JsArray = {
		var problemList: JsArray = new JsArray()
		for(p <- probs) {
				val answers = models.problems.Answer.getByProblemId(p.id) // returns a List[Answer]
				val solution = models.problems.Solution.getByProblemId(p.id) // returns List[Step]
				val pictures = models.problems.Problem.getAllPictures(p.id) // returns List[Picture]
				val cPictures = models.problems.Problem.getContentPictures(p.id) // returns List[Picture]
				val jsonAnswers = JsArray(answers.map(x => models.problems.Answer.toJson(x)))
				val jsonSteps = JsArray(solution.map(x => models.problems.Solution.toJson(x)))
				val jsonPictures = JsArray(pictures.map(x => models.Picture.toJson(x))) // we can instantiate implicit json conversions
				val jsonContentPictures = JsArray(pictures.map(x => models.Picture.toJson(x)))
				val json = Json.obj(
					"id" -> p.id,
					"contents" -> p.contents,
					"topic" -> p.topic,
					"difficulty" -> p.difficulty,
					"pictures" -> jsonPictures,
					"contentpictures" -> jsonContentPictures,
					"answers" -> jsonAnswers,
					"solution" -> jsonSteps)
				problemList = problemList.+:(json)  // TODO: A better solution, but i can't believe this one works....
			}
		problemList
	}
	/* TODO: Answer question why form tuples have to have more than one parameter */
	val topicForm = Form(
		tuple(
			"id" -> default(of[Long], 0L),
			"topic" -> default(of[String], "")))
	def problemsByTopic = Action {
		implicit request => {
			val (id, topic) = topicForm.bindFromRequest.get
			models.problems.Problem.getByTopic(topic) match {
				case List() => BadRequest("Unable to retrieve any problems for Topic: " + topic)
				case problems => Ok(problemsToJson(problems))
			}
		}
	}
}