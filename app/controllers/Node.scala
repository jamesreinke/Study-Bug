package controllers

/* Used for building navigation menus */
object Node {

	abstract class Menu
	case class Link(tag: String, image: String = "", link: play.api.mvc.Call) extends Menu
	case class LinkedLinks(tag: String, image: String = "", links: List[Link]) extends Menu

	/* An example of a profile navigation */
	val profile = List(
		new Link("Profile", "icon-user", routes.Application.index()),
		new Link("Logout", "icon-key", routes.Authentication.logout()))

	/* An example of a sidebar navigation */
	val sidebar = List[Menu](Application.iLink)

}