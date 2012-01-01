package net.solarfoundry.upfolder
package impl

import org.scalatest.FunSuite
import org.scalatest.Assertions
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import events._

class TestStorage extends MemoryStorage with EventGeneratingStorage with StorageEventsCollecting

/**
 * Event matchers DSL for testing.
 * 
 * Given an instance of EventGeneratingStorage called 'storage', available assertions are:
 * "storage events contain (StorageUpdated)"
 * "storage events (contain (StorageUpdated) and contain (ResourceCreated))"
 * 
 * Additional handy operations:
 * "storage clear events"
 * 
 * Note: syntactic sugar allows to type "StorageUpdated" where event types would be needed, but 
 * they are matched always and only by name.
 * 
 * How matchers are designed: they work in multiple steps:
 * 1. access the received events; the trait StorageEventsCollecting is known to collect them in a list
 *    so first the Storage instance is implicitly converted to EventsWrapper
 * 
 * 2. instances of EvenMatcher trait are created and used to do the verification work
 *    these instances are created when "interpreting" the event matchers DSL
 */
trait EventMatchers {
  
  private def verifyAndCast(s: Storage) = {
    require(s.isInstanceOf[EventGeneratingStorage])
    require(s.isInstanceOf[StorageEventsCollecting], "trait StorageEventsCollecting must be mixed in")
    s.asInstanceOf[StorageEventsCollecting]
  }
  
  private def extractEvents(s: Storage) = {
    val sec = verifyAndCast(s)
    sec.receivedEvents.toSeq
  }

  implicit def storageEventsWrapper(s: Storage) = {
    new EventsWrapper(extractEvents(s))
  } 
  
  implicit def storageWrapper(s: Storage) = {
    new TestfulRichStorage(verifyAndCast(s))
  }

  class EventsWrapper(eventWorkingSet: Seq[Event]) {
    def events(m: EventMatcher) = m.verifyEvents(eventWorkingSet)
  }
  
  trait RichStorageArgument
  object events extends RichStorageArgument
    
  class TestfulRichStorage(s: StorageEventsCollecting) {
    def clear(what: RichStorageArgument) = {
      if (what == events) s.receivedEvents.clear()
    }
  }

  def contain(e: Event): EventMatcher = new ContainEventMatcher(e)

  trait EventMatcher {
    def verifyEvents(events: Seq[Event])
    def and(em: EventMatcher): EventMatcher = new AndEventMatcher(this, em)
  }

  final class ContainEventMatcher(e: Event) extends EventMatcher with Assertions {
    override def verifyEvents(events: Seq[Event]) {
      if (!events.find(_.name == e.name).isDefined)
        fail("expected event not found: "+ e.name)
    }
  }

  final class AndEventMatcher(first: EventMatcher, second: EventMatcher) extends EventMatcher {
    override def verifyEvents(events: Seq[Event]) {
      first.verifyEvents(events)
      second.verifyEvents(events)
    }
  }
}

@RunWith(classOf[JUnitRunner])
class EventGeneratingStorageTest extends FunSuite with ShouldMatchers with EventMatchers {
  import StorageEvents._

  test("creating resource generates StorageUpdated and ResourceCreated") {
    val storage = new TestStorage
    storage.create("/some/path", "SomeName")
    storage events (contain (StorageUpdated) and contain (ResourceCreated))
  }

  test("deleting resource generates StorageUpdated and ResourceDeleted") {
    val storage = new TestStorage
    val handle = storage.create("/some/path", "SomeName")
    
    storage clear events
    storage.delete(handle)
    storage events (contain (StorageUpdated) and contain (ResourceDeleted))
  }
}
