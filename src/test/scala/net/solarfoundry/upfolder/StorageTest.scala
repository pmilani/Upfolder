package net.solarfoundry.upfolder

import java.util.UUID
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import org.specs.specification._
import org.specs.mock.Mockito
import java.util.ConcurrentModificationException

@RunWith(classOf[JUnitRunner])
class StorageTest extends FunSuite with Mockito with DefaultExampleExpectationsListener {
  
  val TestHandle = new Handle("40ca4c24-6663-40ee-ab73-99f8fe3f07c4")

  class DummyStorage extends Storage {
    def create(path: String, name: String): Handle = null
    def delete(handle: Handle) = ()
    def find(f: (String, String) => Boolean): Iterable[Handle] = null
    def apply(handle: Handle): Accessor = null
  }

  test("find by path and name should use find with a predicate") {
    val storage = spy(new DummyStorage)
    storage.find("/path/to/stuff","data1")
    there was one(storage).find(any)
  }

  test("create unique resource on storage") {
    val storage = spy(new DummyStorage)
    storage.create("/path/to/stuff","data1") returns TestHandle
    storage.find(any) returns Seq(TestHandle)

    storage.createUnique("/path/to/stuff","data1")
    
    there was one(storage).create("/path/to/stuff","data1")
    there was one(storage).find(any)
  }

  test("fail to create unique resource when same (path,name) exist") {
    val storage = spy(new DummyStorage)
    storage.create("/path/to/stuff","data1") returns TestHandle
    storage.find(any) returns Seq(TestHandle, Handle(UUID.randomUUID))  // simulate concurrent creation

    intercept[ConcurrentModificationException] {
      storage.createUnique("/path/to/stuff","data1")
    }
    
    there was one(storage).create("/path/to/stuff","data1")
  }
}