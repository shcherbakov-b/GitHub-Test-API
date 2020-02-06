package internal.proj.errors

trait DomainError extends Exception {
  override def getMessage: String = message
  def message: String
}
object DomainError {
  case class OrganizationNotFound() extends DomainError {
    override def message: String = s"Organization not found"
  }
  case class BadResponse() extends DomainError {
    override def message: String = s"Bad response"
  }
}
