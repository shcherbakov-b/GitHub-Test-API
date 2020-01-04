package internal.proj.errors

object DomainErrors {

  case class CannotDecode(msg: String) extends Throwable

}
