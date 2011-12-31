package net.solarfoundry.upfolder.impl

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CreateStorageTest extends FunSuite with ShouldMatchers {
  val location = new File("/tmp")

  test("in memory storage") {
    CreateStorage.inMemory() 
        .isInstanceOf[MemoryStorage] should be(true) 
  }
  
  test("in memory storage, event generating") {
    CreateStorage.eventGenerating.inMemory()
        .isInstanceOf[EventGeneratingStorage] should be(true)
  }
  
  test("in memory storage, event generating, with logging") {
    CreateStorage.eventGenerating.andLogging.inMemory()
        .isInstanceOf[StorageEventsLogging] should be(true)
  }

  test("in memory storage, event generating, with collecting") {
    CreateStorage.eventGenerating.andCollecting.inMemory()
        .isInstanceOf[StorageEventsCollecting] should be(true)
  }

  test("filesystem storage") {
    CreateStorage.onFilesystem(location)
        .isInstanceOf[FilesystemStorage] should be(true) 
  }
  
  test("filesystem storage, event generating") {
    CreateStorage.eventGenerating.onFilesystem(location)
        .isInstanceOf[EventGeneratingStorage] should be(true)
  }
  
  test("filesystem storage, event generating, with logging") {
    CreateStorage.eventGenerating.andLogging.onFilesystem(location)
        .isInstanceOf[StorageEventsLogging] should be(true)
  }

  test("filesystem storage, event generating, with collecting") {
    CreateStorage.eventGenerating.andCollecting.onFilesystem(location)
        .isInstanceOf[StorageEventsCollecting] should be(true)
  }
}