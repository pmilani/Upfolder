package net.solarfoundry.upfolder
package impl

import events._
import java.util.UUID
import java.io.InputStream
import com.sun.tools.javac.util.ListBuffer

trait StorageEventsLogging extends EventSignaling {
  abstract override def occurred(e: Event)(implicit eventInfo: Map[String,String] = Map.empty) {
    super.occurred(e)(eventInfo)
    if (eventInfo.isEmpty)
      println("time=%d : %s ".format(e.when, e.name))
    else
      println("time=%d : %s { %s } ".format(e.when, e.name, eventInfo.mkString(",")))
  }
}

trait StorageEventsCollecting extends EventSignaling {
  import scala.collection.mutable.ListBuffer
  val receivedEvents: ListBuffer[Event] = new ListBuffer

  abstract override def occurred(e: Event)(implicit eventInfo: Map[String,String] = Map.empty) {
    receivedEvents += e
  }
}

/**
 * Stackable event generation for Storage
 */
trait EventGeneratingStorage extends Storage with EventSignaling {
  import StorageEvents._
  
  override abstract def create(path: String, name: String): Handle = {
    val handle = super.create(path, name)
    occurred(ResourceCreated)(Map("handle"->handle.id.toString))
    occurred(StorageUpdated)
    handle
  }
  
  override abstract def delete(handle: Handle) {
    super.delete(handle)
    occurred(ResourceDeleted)(Map("handle"->handle.id.toString))
    occurred(StorageUpdated)
  }
}

/**
 * Stackable event generation for Accessor
 */
trait EventGeneratingAccessor extends Accessor with EventSignaling {
  import StorageEvents._
  
  override abstract def bytes: Array[Byte] = {
    val result = super.bytes
    occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"bytes read"))
    result
  }

  override abstract def bytes_=(value: Array[Byte]) = {
    super.bytes_=(value)
    occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"bytes written"))
    occurred(StorageUpdated)
  }
  
  override abstract def inputStream[A](code: InputStream => A) = {
    val result = super.inputStream(code)
    occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"stream read"))
    result
  }
}