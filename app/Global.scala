import play._
import models._
import problems._
import users._

class Global extends GlobalSettings {

	/* Initialize our databases */
	override def onStart(app: Application) {
			/*
			println("Initializing databases")
			Topic.init
			Subtopic.init
			Answer.init
			Authentication.init
			Picture.init
			Problem.init
			Solution.init
			*/
	}
}
