package net.solarfoundry.upfolder
package impl

import java.io.File

trait CanBuildStorage {
  def inMemory(): Storage
  def onFilesystem(location: File): Storage
}

/**
 * Factory provides some syntactic sugar.
 * For full flexibility and more advanced stackability, instantiate a Storage yourself and mix in desired traits
 */
object CreateStorage extends CanBuildStorage {
  
  def inMemory() = new MemoryStorage
  def onFilesystem(location: File) = new FilesystemStorage(location)
  
  def eventGenerating = new BuilderWithEvents
  
  class BuilderWithEvents extends CanBuildStorage {
    
    def andLogging: CanBuildStorage = return new BuilderWithEventLogging
    
    def andCollecting: CanBuildStorage = return new BuilderWithEventCollecting

    // TODO an event notifying trait needs to be mixed in
    def inMemory() = new MemoryStorage with EventGeneratingStorage
    def onFilesystem(location: File) = new FilesystemStorage(location) with EventGeneratingStorage
  }
  
  private class BuilderWithEventLogging extends CanBuildStorage {
    def inMemory() = new MemoryStorage with EventGeneratingStorage with StorageEventsLogging
    def onFilesystem(location: File) = new FilesystemStorage(location) with EventGeneratingStorage with StorageEventsLogging
  }

  private class BuilderWithEventCollecting extends CanBuildStorage {
    def inMemory() = new MemoryStorage with EventGeneratingStorage with StorageEventsCollecting
    def onFilesystem(location: File) = new FilesystemStorage(location) with EventGeneratingStorage with StorageEventsCollecting
  }
}