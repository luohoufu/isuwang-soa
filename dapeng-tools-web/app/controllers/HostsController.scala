package controllers

import javax.inject._

import com.isuwang.dapeng.tools.helpers.{RouteInfoHelper, RequestExampleHelper, MetaInfoHelper}
import module.ServiceInfo
import play.api.mvc._

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
    val serviceInfos: scala.collection.mutable.Map[String, ListBuffer[ServiceInfo]] = scala.collection.mutable.Map[String, ListBuffer[ServiceInfo]]()
    serviceInfos += ("192.0.0.1" -> ListBuffer(new ServiceInfo("service1","1.0.0","8080"),new ServiceInfo("service2","1.0.0","8080"),new ServiceInfo("service3","1.0.0","8080")))
    serviceInfos += ("192.0.0.2" -> ListBuffer(new ServiceInfo("service1","1.0.0","8080"),new ServiceInfo("service2","1.0.0","8080"),new ServiceInfo("service3","1.0.0","8080")))
    serviceInfos += ("192.0.0.3" -> ListBuffer(new ServiceInfo("service1","1.0.0","8080"),new ServiceInfo("service2","1.0.0","8080"),new ServiceInfo("service3","1.0.0","8080")))

    //    val serviceInfos = ZookeeperHelper.getInfos()
    println("serviceInfos: " + serviceInfos.size)
    Ok(views.html.hosts.render(serviceInfos.toMap))
  }

  //通过服务名和版本号，获取元信息:
  def metadata = Action{
    MetaInfoHelper.getService()
    Ok("123")
  }

   def jsonResult = Action{
    RequestExampleHelper.getRequestJson()
    Ok("123")
  }

  def xmlResult = Action{
    RequestExampleHelper.getRequestXml()
    Ok("123")
  }

  def jsonResultWithPara = Action{
    RequestExampleHelper.getRequestJson()
    Ok("123")
  }

  def xmlResultWithPara = Action{
    RequestExampleHelper.getRequestXml()
    Ok("123")
  }

  //通过系统参数，xml文件，调用指定服务器的服务并打印结果:
  def routeInfo = Action{
    RouteInfoHelper.routeInfo()
    Ok("123")
  }
}
