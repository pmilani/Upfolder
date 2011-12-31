package net.solarfoundry.upfolder

import javax.servlet.http.{HttpServlet,HttpServletRequest,HttpServletResponse}
import javax.servlet.ServletConfig
import java.io.File
import net.solarfoundry.upfolder.impl.CreateStorage
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.util.ConcurrentModificationException

class StorageController extends HttpServlet {
  final val StorageTypeKey = "storageType"
  final val StorageLocationKey = "storageLocation"

  final val HandleHeaderName = "X-Upfolder-Handle"
  final val HandleParamName = "handle"
    
  var storage: Storage = _
    
  // TODO implement this for caching
  override def getLastModified(req: HttpServletRequest) = -1
  
  override def init(config: ServletConfig) {
    val storageType = config.getInitParameter(StorageTypeKey)
    storage=storageType.toLowerCase match {
      case "memory" => CreateStorage.inMemory()
      case "filesystem" => {
        val storageLocation = getServletConfig.getInitParameter(StorageLocationKey)
        CreateStorage.onFilesystem(new File(storageLocation))
      }
      case _ => throw new IllegalArgumentException("Invalid storage type: "+ storageType)
    }
  }

  def pathAndName(req: HttpServletRequest) = (FilenameUtils.getBaseName(req.getPathInfo), FilenameUtils.getName(req.getPathInfo))

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val handle = if (req.getParameter(HandleParamName) != null) {
      new Handle(req.getParameter(HandleParamName))
    } else {
      val (path, name) = pathAndName(req)
      storage.find(path, name).head
    }
    
    storage(handle).inputStream(stream => IOUtils.copy(stream, resp.getOutputStream()))
    resp.getOutputStream().flush()
  }

  override def doPut(req: HttpServletRequest, resp: HttpServletResponse) {
    val (path, name) = pathAndName(req)
    val found = storage.find(path, name)
    
    try {
      val handle = if (found.isEmpty) {
        storage.createUnique(path, name)
      } else {
        found.head
      }
      // TODO range headers?
      storage(handle).outputStream(outStream => IOUtils.copy(req.getInputStream(), outStream))
      resp.setHeader(HandleHeaderName, handle.toString)
      resp.setStatus(HttpServletResponse.SC_CREATED)
    } catch {
      case e:ConcurrentModificationException => resp.setStatus(HttpServletResponse.SC_CONFLICT)
    }
  }
  
  override def doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
    val handle = if (req.getParameter(HandleParamName) != null) {
      new Handle(req.getParameter(HandleParamName))
    } else {
      val (path, name) = pathAndName(req)
      storage.find(path, name).head
    }
    storage.delete(handle)
    resp.setStatus(HttpServletResponse.SC_RESET_CONTENT)
  }
}