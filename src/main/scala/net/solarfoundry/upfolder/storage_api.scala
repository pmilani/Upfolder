package net.solarfoundry.upfolder

import java.util.UUID
import java.io.InputStream
import java.io.OutputStream
import java.util.ConcurrentModificationException

case class Handle(id: UUID) {
  def this(idString: String) = this(UUID.fromString(idString))
  
  override def toString() = id.toString()
}

trait Storage {
  def create(path: String, name: String): Handle
  def delete(handle: Handle)
  
  def createUnique(path: String, name: String) = {
    val handle = create(path, name)
    if (find(path, name).size != 1) {
      delete(handle)
      throw new ConcurrentModificationException
    }
    handle
  }

  /**
   * Multiple entries are allowed with same (path,name)
   * f(path, name) returns true when 
   */
  def find(f: (String, String) => Boolean): Iterable[Handle]
  
  def find(path: String = null, name: String = null): Iterable[Handle] = find((p,n) => {
    val pathCond = Option(path).map(_.equals(p)).orElse(Some(true))
    val nameCond = Option(name).map(_.equals(n)).orElse(Some(true))
    pathCond.get && nameCond.get
  })
  
  def apply(handle: Handle): Accessor
}

trait Accessor {
  val handle: Handle
  
  def bytes: Array[Byte]
  def bytes_=(value: Array[Byte])
  
  /**
   * Opens an input stream to read data, executes your code and then ensures the stream will be closed
   */
  def inputStream[A](code: InputStream => A): A

  /**
   * Creates an output stream to write data, executes your code and then ensures the stream will be closed
   */
  def outputStream[A](code: OutputStream => A): A
}
