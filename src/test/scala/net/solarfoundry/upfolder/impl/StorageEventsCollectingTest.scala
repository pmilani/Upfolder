package net.solarfoundry.upfolder.impl

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import net.solarfoundry.upfolder.events.Event
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfter

/**
 * EventMatchers rely on StorageEventsCollecting, therefore this test must not use them.
 */
@RunWith(classOf[JUnitRunner])
class StorageEventsCollectingTest extends FunSuite with ShouldMatchers with BeforeAndAfter {

  val collector = new StorageEventsCollecting {
    def fireEvent(e: Event) = occurred(e)
  }

  after {
    collector.receivedEvents.clear()
  }
  
  test("collecting a sequence of events") {
    collector.receivedEvents should have length (0)
    collector.fireEvent(Event("DummyEvent"))
    collector.fireEvent(Event("DummierEvent"))
    collector.receivedEvents should have length (2)
    
    collector.receivedEvents(0).name should equal ("DummyEvent")
    collector.receivedEvents(1).name should equal ("DummierEvent")
  }
  
  test("clearing collected events") {
    collector.receivedEvents should have length (0)    
    collector.fireEvent(Event("DummyEvent"))
    collector.fireEvent(Event("DummierEvent"))
    
    collector.receivedEvents should have length (2)
    collector.receivedEvents.clear()
    collector.receivedEvents should have length (0)    
  }
}