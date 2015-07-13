package controllers

/* Used for building navigation menus */
object Node {

	abstract class Menu
	case class Link(tag: String, image: String = "", link: play.api.mvc.Call) extends Menu
	case class LinkedLinks(tag: String, image: String = "", links: List[Link]) extends Menu



	/* Sidebar navigation */
	val index = new Link("Home", "fa fa-home", routes.Application.index())
	val database = new Link("Problems", "fa fa-database", routes.Problem.getPage())
	val topics = new Link("Topics", "", routes.Topic.getPage())
	val subtopics = new Link("Subtopics", "", routes.Subtopic.getPage())
	val categories = new LinkedLinks("Categories", "fa fa-university", List(topics, subtopics))
	val sidebar = List[Menu](index, database, categories)

	/* Profile navigation */
	val stats = new Link("Statistics", "fa fa-calculator", routes.Profile.stats())
	val prof = new Link("Profile", "fa fa-user", routes.Profile.index())
	val logout = new Link("Logout", "fa fa-key", routes.Authentication.logout)
	val profile = List[Link](prof, stats, logout)

}