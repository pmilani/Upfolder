package net.solarfoundry

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import net.solarfoundry.sampleapp.SampleAppController
import org.eclipse.jetty.webapp.WebAppContext

object JettyMain {
  
  def main(args : Array[String]) {
    val server = new Server(8080)
    val context = new WebAppContext("src/main/webapp", "/")
    
    context.addServlet(new ServletHolder(classOf[SampleAppController]), "/*")
    
    server.setHandler(context)
    server.start()
    server.join()
  }

}
