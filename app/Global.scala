import play._
import models.problems._
import models.users._

class Global extends GlobalSettings {

	/* Initialize our databases */
	override def onStart(app: Application) {

		Topic.init
		Subtopic.init
		Solution.init
		Answer.init
		Authentication.init
		Problem.init
		Authentication.init
	}
}