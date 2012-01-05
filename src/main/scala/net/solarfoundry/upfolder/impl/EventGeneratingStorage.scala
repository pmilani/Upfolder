package net.solarfoundry.upfolder
package impl

import events._
import java.util.UUID
import java.io.InputStream
import java.io.OutputStream

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
  import scala.collection.mutable.{Map => MutableMap}
  
  val receivedEvents: ListBuffer[Event] = new ListBuffer
  private val receivedEventInfo: MutableMap[UUID, EventInfo] = MutableMap()
  
  def receivedEventInfo(e: Event): Map[String,String] = receivedEventInfo(e.id)

  def resetReceivedEvents() {
    receivedEvents.clear()
    receivedEventInfo.clear()
  }
  
  abstract override def occurred(e: Event)(implicit eventInfo: Map[String,String] = Map.empty) {
    receivedEvents += e
    if (!eventInfo.isEmpty) 
      receivedEventInfo(e.id) = eventInfo
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
  
  override abstract def apply(handle: Handle): Accessor = new EventGeneratingAccessor(super.apply(handle), this)
}

/**
 * Event generation for Accessor, delegating to original accessor
 */
class EventGeneratingAccessor(delegate: Accessor, signaling: EventSignaling) extends Accessor {
  import StorageEvents._
  
  def handle = delegate.handle
  
  def bytes: Array[Byte] = {
    val result = delegate.bytes
    signaling.occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"bytes read"))
    result
  }

  def bytes_=(value: Array[Byte]) {
    delegate.bytes_=(value)
    signaling.occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"bytes written"))
    signaling.occurred(StorageUpdated)
  }
  
  def inputStream[A](code: InputStream => A) = {
    val result = delegate.inputStream(code)
    signaling.occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"stream read"))
    result
  }
  
  def outputStream[A](code: OutputStream => A) = {
    val result = delegate.outputStream(code)
    signaling.occurred(DataAccess)(Map("handle"->handle.id.toString, "op"->"stream write"))
    signaling.occurred(StorageUpdated)
    result
  }
}