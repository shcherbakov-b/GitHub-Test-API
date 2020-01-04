package internal.proj.models

case class Contributor(login: String, contributions: Int) {

  def merge(that: Contributor): Contributor = {
    require(that.login == this.login)
    copy(contributions = this.contributions + that.contributions)
  }
}
