package controllers
import javax.inject._

import play.api.mvc._
import play.api.db._
/**
 *
 * @author on 2016/7/20 18:14
 */
@Singleton
class JdbcController @Inject()(db: Database) extends Controller {

  def add = Action {
    val outString = "jdbc add"
    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      stmt.execute("INSERT INTO skills (name, code, level, `created_at`, `updated_at`) VALUES ('as2', 'qw2', '1', '2016-07-19 19:35:04', '2016-07-19 19:39:42'); ")
    } finally {
      conn.close()
    }
    Ok(outString)
  }



  def delete = Action {
    val outString = "jdbc delete "
    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      val rs = stmt.executeUpdate("delete from skills where id=1023 ")

    } finally {
      conn.close()
    }
    Ok(outString)
  }

  def update = Action {
    val outString = "jdbc update "
    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      val rs = stmt.executeUpdate("update skills set name='testPlay' where id=1024")

    } finally {
      conn.close()
    }
    Ok(outString)
  }

  def query = Action {
    var outString = "jdbc query "
    val conn = db.getConnection()

    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT name as testkey from skills where id = 1024")

      while (rs.next()) {
        outString += rs.getString("testkey")
      }
    } finally {
      conn.close()
    }
    Ok(outString)
  }

}