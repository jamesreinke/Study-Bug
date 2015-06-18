package controllers

/* Used for building navigation menus */
object Node {

	abstract class Menu
	case class Link(tag: String, image: String = "", link: play.api.mvc.Call) extends Menu
	case class LinkedLinks(tag: String, image: String = "", links: List[Link]) extends Menu


	val profile = List[Link](Profile.iLink, Profile.sLink)

	/* An example of a sidebar navigation */
	val sidebar = List[Menu](Application.iLink, Problem.dLink, Problem.cLink)

}