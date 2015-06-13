package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index(a: String) = Action {

  	implicit request => {

  		if(!Authentication.auth(request)) Ok(views.html.pages.login(Authentication.userForm, msg = a))
  		else Ok("Well this is all you get...")
  		
  	}
    
  }

}
