package internal.proj.errors

sealed trait DomainError extends Exception {
  def message: String
  override def getMessage: String = message
}

case class DecodeFailed(message: String) extends DomainError


