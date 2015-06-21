import play._
import models.problems._
import models.users._

class Global extends GlobalSettings {

	/* Initialize our databases */
	override def onStart(app: Application) {
		println("Initializing databases")
		Topic.init
		Subtopic.init
		Answer.init
		Authentication.init

	}
}
