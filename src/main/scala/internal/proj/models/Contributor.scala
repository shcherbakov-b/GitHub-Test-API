package internal.proj.models

case class Contributor(login: String, contributions: Int) extends Ordered[Contributor] {

  def merge(that: Contributor): Contributor =
    copy(contributions = this.contributions + that.contributions)

  override def compare(that: Contributor): Int =
    that.contributions.compareTo(this.contributions)
}
