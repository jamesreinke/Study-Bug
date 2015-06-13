package views.html.forms
import views.html.helper.{FieldConstructor, FieldElements}

/* We override the default field constructor with this new one as defined by the form parameteres in form.scala.html */
object Custom {
	implicit val fieldConstructor = new FieldConstructor {
		def apply(elements: FieldElements) = formField(elements)
	}
}