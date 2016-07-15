package controllers

import javax.inject._

import module.ServiceInfo
import play.api.mvc._
import services.Counter
import util.ZookeeperHelper

import scala.collection.mutable.ListBuffer

@Singleton
class HostsController @Inject() extends Controller {

  def listHosts = Action {
    //    ZookeeperHelper.getInfos()
    val ok = Ok("Hello world!")
    //    val notFound = NotFound                            404
    //    val pageNotFound = NotFound(<h1>Page not found</h1>)   404
    //    val oops = InternalServerError("Oops")  500
    //    val anyStatus = Status(488)("Strange response type")  488
    //    Ok(views.html.index("服务治理平台."))
    //    Redirect("/count")
//    val result2 = Ok(<h3>Hello World!</h3>).as(HTML)
//    ok
//    result2
    val serviceInfos = ZookeeperHelper.getInfos()
    println("serviceInfos: " + serviceInfos.size)
    Ok(views.html.hosts.render(serviceInfos))
  }
}
