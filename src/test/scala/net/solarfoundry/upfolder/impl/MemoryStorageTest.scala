package net.solarfoundry.upfolder.impl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.UUID
import net.solarfoundry.upfolder.Storage
import net.solarfoundry.upfolder.Handle
import net.solarfoundry.upfolder.Accessor
import org.apache.commons.io.IOUtils

@RunWith(classOf[JUnitRunner])
class MemoryStorageTest extends FunSuite with ShouldMatchers {

  test("instantiate memory storage") {
    val storage = new MemoryStorage
    storage should not be null
  }
  
  test("create resource should store the metadata and return handle") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    handle should not be null
    handle.id should not be null
    storage.metadata should contain key (handle)
    storage.metadata(handle).originalPath should equal("/path/to/stuff")
    storage.metadata(handle).originalName should equal("myfile")
    storage.items should not contain key (handle)
  }

  test("create multiple resources") {
    val storage = new MemoryStorage
    val handle1 = storage.create("/path/to/somestuff","myfile")
    val handle2 = storage.create("/path/to/otherstuff","otherfile")
    handle1 should not equal(handle2)
    storage.metadata should have size(2)
  }
  
  test("create multiple resources with same (path,name)") {
    val storage = new MemoryStorage
    val handle1 = storage.create("/path/to/somestuff","myfile")
    val handle2 = storage.create("/path/to/somestuff","myfile")
    handle1 should not equal(handle2)
    storage.metadata should have size(2)
    storage.metadata(handle1).originalPath should equal(storage.metadata(handle2).originalPath)
    storage.metadata(handle1).originalName should equal(storage.metadata(handle2).originalName)
  }
  
  test("get accessor for stored resource") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    
    val accessor = storage(handle)
    accessor should not be null
    accessor.isInstanceOf[Accessor] should be (true)
  }

  test("fail to get accessor for non existent resource") {
    val storage = new MemoryStorage
    intercept[IllegalArgumentException] {
      storage(Handle(UUID.randomUUID))
    }
  }
  
  test("fail to get accessor after resource was deleted") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    storage.delete(handle)
    intercept[IllegalArgumentException] {
      storage(handle)
    }
  }

  test("fail to access underlying data after resource was deleted") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    val accessor = storage(handle)
    storage.delete(handle)
    intercept[IllegalArgumentException] {
      accessor.bytes
    }
  }

  test("update bytes via accessor") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    
    storage(handle).bytes = Array(1,2,3,4)
    storage.items(handle) should equal(Array(1,2,3,4))
  }

  test("read bytes via accessor") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    
    storage.items(handle) = Array(5,6,7,8)
    storage(handle).bytes should equal(Array(5,6,7,8))
  }

  test("read stream via accessor") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    
    storage.items(handle) = Array(5,6,7,8)
    val streamedBytes = storage(handle).inputStream(stream => IOUtils.toByteArray(stream))
    streamedBytes should equal(Array(5,6,7,8))
  }
  
  test("delete should not fail when handle not found") {
    val storage = new MemoryStorage
    val handle = Handle(UUID.randomUUID())
    
    require(!storage.items.contains(handle))
    storage.delete(handle)
  }
  
  test("delete when only metadata present") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    
    require(!storage.items.contains(handle))
    require(storage.metadata.contains(handle))
    storage.delete(handle)
    storage.metadata should not contain key (handle)
  }
  
  test("delete item and metadata from storage") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    storage(handle).bytes = Array(1,2,3,4)
    
    require(storage.items.contains(handle))
    require(storage.metadata.contains(handle))
    storage.delete(handle)
    storage.metadata should not contain key (handle)
    storage.items should not contain key (handle)
  }

  test("delete is idempotent") {
    val storage = new MemoryStorage
    val handle = storage.create("/path/to/stuff","myfile")
    
    require(storage.metadata.contains(handle))
    storage.delete(handle)
    storage.delete(handle)
    storage.metadata should not contain key (handle)
  }

  test("find resources by predicate") {
    val storage = CreateStorage.inMemory()
    val handle1 = storage.create("/path/to/stuff","myfile")
    val handle2 = storage.create("/path/to/stuff","myotherfile")
    val handle3 = storage.create("/path/to/stuff","notmyfile")
    
    val foundHandles = storage.find((_,name) => name.startsWith("my")) 
    foundHandles should (have size (2) and contain (handle1) and contain (handle2))
  }
  
  test("find resources by name") {
    val storage = CreateStorage.inMemory()
    val handle1 = storage.create("/path/to/somestuff","myfile")
    val handle2 = storage.create("/path/to/otherstuff","myfile")
    val handle3 = storage.create("/path/to/stuff","notmyfile")
    
    val foundHandles = storage.find(name="myfile") 
    foundHandles should (have size (2) and contain (handle1) and contain (handle2))
  }

  test("find resources by path") {
    val storage = CreateStorage.inMemory()
    val handle1 = storage.create("/path/to/somestuff","myfile")
    val handle2 = storage.create("/path/to/otherstuff","myfile")
    val handle3 = storage.create("/path/to/somestuff","notmyfile")
    
    val foundHandles = storage.find(path="/path/to/somestuff") 
    foundHandles should (have size (2) and contain (handle1) and contain (handle3))
  }
  
  test("find resources by path and name") {
    val storage = CreateStorage.inMemory()
    val handle1 = storage.create("/path/to/somestuff","myfile")
    val handle2 = storage.create("/path/to/otherstuff","myfile")
    val handle3 = storage.create("/path/to/somestuff","notmyfile")
    
    val foundHandles = storage.find(path="/path/to/somestuff",name="myfile") 
    foundHandles should (have size (1) and contain (handle1))
  }

  test("scenario run: create, access data, find, delete") {
    val storage = CreateStorage.eventGenerating.andLogging.inMemory().asInstanceOf[MemoryStorage]
    val handle = storage.create("/path/to/stuff","myfile")
    storage(handle).bytes = Array(1,2,3,4)
    storage.create("/path/to/stuff","myotherfile")
    storage.create("/path/to/stuff","notmyfile")
    //checkpoint
    storage.metadata should have size (3)
    storage.items should have size (1)
    
    val foundHandles = storage.find((_,name) => name.startsWith("my"))
    foundHandles.foreach(storage.delete(_))
    //checkpoint
    storage.metadata should have size (1)
    storage.items.isEmpty should be (true)
    
    val notmyfileHandle = storage.find(name="notmyfile").head
    storage.delete(notmyfileHandle)
    //checkpoint
    storage.metadata.isEmpty should be (true)
  }
}