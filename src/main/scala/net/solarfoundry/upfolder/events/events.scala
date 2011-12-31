package net.solarfoundry.upfolder
package events

import java.util.UUID

case class Event(name:String, id:UUID = UUID.randomUUID, when: Long = System.currentTimeMillis)

object StorageEvents {
  
  def StorageUpdated = Event("STORAGE_UPDATED")
  
  def ResourceCreated = Event("RESOURCE_CREATED")
  def ResourceDeleted = Event("RESOURCE_DELETED")
  
  def DataAccess = Event("DATA_ACCESS")
}

trait EventSignaling {
  def occurred(e: Event)(implicit eventInfo: Map[String,String] = Map.empty) {}
}
