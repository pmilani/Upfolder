package net.solarfoundry.upfolder
package impl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import events._

class TestStorage extends MemoryStorage with EventGeneratingStorage with StorageEventsCollecting

trait EventMatchers {
  
  implicit def StorageEventsWrapper(s: TestStorage) = new StorageWrapper(s)

  class StorageWrapper(s: StorageEventsCollecting) {
    def events(m: EventMatcher) = m.verifyStorage(s)
  }

  def contain(e: Event): EventMatcher = new ContainEventMatcher(e)

  trait EventMatcher {
    def verifyStorage(s: StorageEventsCollecting) {}
    def and(em: EventMatcher): EventMatcher = new AndMatcher(em)
  }

  final class ContainEventMatcher(e: Event) extends EventMatcher with ShouldMatchers {
    override def verifyStorage(s: StorageEventsCollecting) {
      s.receivedEvents.find(_.name == e.name) should not be null
    }
  }

  final class AndMatcher(otherMatcher: EventMatcher) extends EventMatcher {
    override def verifyStorage(s: StorageEventsCollecting) {
      otherMatcher.verifyStorage(s)
    }
  }
  
}

@RunWith(classOf[JUnitRunner])
class EventGeneratingStorageTest extends FunSuite with ShouldMatchers with EventMatchers {
  import StorageEvents._

  test("generates StorageUpdate") {
    val storage = new TestStorage
    storage.create("/some/path", "SomeName")
    storage events (contain (StorageUpdated) and contain (ResourceCreated))
  }
  
  
}
