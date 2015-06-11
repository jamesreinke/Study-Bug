import play._

class Global extends GlobalSettings {

	/* Initialize our databases */
	override def onStart(app: Application) {

		models.problems.Topic.init
		
	}
}