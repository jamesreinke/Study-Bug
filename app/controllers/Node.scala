package controllers

/* Used for building navigation menus */
object Node {

	sealed abstract class Node
	case class Leaf(tag: String, link: String) extends Node 
	case class Tree(tag: String, nodes: List[Node]) extends Node

}