package controllers

/* Used for building navigation menus */
object Node {

	abstract class Menu
	case class Link(tag: String, link: play.api.mvc.Call, image: String = "") extends Menu
	case class LinkedLinks(tag: String, image: String = "", links: List[Link]) extends Menu

	/* An example of a profile navigation */
	val profile = List(
		new Link("Profile", routes.Application.index(), "icon-user"),
		new Link("Logout", routes.Authentication.logout(), "icon-key"))

	/* An example of a sidebar navigation */
	val sidebar = List[Menu](
		new Link("Dashboard", routes.Application.index(), "icon-home"),
		new LinkedLinks("See More", "icon-pointer", List(
			new Link("This is more", routes.Application.index(), "icon-home"),
			new Link("This is also more", routes.Application.index(), "icon-home"))))

}