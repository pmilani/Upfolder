package net.solarfoundry.upfolder
package impl

import java.util.UUID
import java.io.InputStream
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.FileOutputStream
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FileUtils


private trait Metadata {
  case class ItemMetadata(originalPath: String, originalName: String, createdAt: Long = System.currentTimeMillis)
  
  import scala.collection.mutable.Map
  val metadata: Map[Handle, ItemMetadata] = Map()
  
  def find(f: (String, String) => Boolean) = {
    for ((handle, m) <- metadata if (f(m.originalPath, m.originalName))) yield handle
  }
  
  final def requireHandleManaged(handle: Handle) {
    require(metadata.contains(handle), "handle not found")
  }
  
  final def handleRequired[A](handle: Handle)(f: => A): A = {
    requireHandleManaged(handle)
    f
  }
}

/**
 * Currently implemented as mutable state object.
 * Another valuable idea, to implement with immutable state and transformations, also versioning the current state
 */
private[impl] class MemoryStorage extends Storage with Metadata {

  class MemoryAccessor(override val handle: Handle) extends Accessor {
    requireHandleManaged(handle)
    
    def bytes = handleRequired(handle) {
      items.getOrElse(handle, null)
    }
    
    def bytes_=(value: Array[Byte]) = handleRequired(handle) {
      items(handle) = value
    }
    
    def inputStream[A](code: InputStream => A): A = handleRequired(handle) {
      val inputStream = new ByteArrayInputStream(bytes)
      code(inputStream)
    }
    
    def outputStream[A](code: OutputStream => A): A = handleRequired(handle) {
      val outputStream = new ByteArrayOutputStream
      val result = code(outputStream)
      bytes = outputStream.toByteArray()
      result
    }
  }

  import scala.collection.mutable.Map
  val items: Map[Handle, Array[Byte]] = Map()

  override def create(path: String, name: String): Handle = {
    val handle = Handle(UUID.randomUUID)
    metadata(handle) = ItemMetadata(path, name)
    handle
  }

  override def delete(handle: Handle) = {
    items.remove(handle)
    metadata.remove(handle)
  }
  
  override def apply(handle: Handle): Accessor = new MemoryAccessor(handle)

}

/**
 * Impl that stores as files, named by the Handle.id
 * FIXME metadata is lost on VM exit
 * TODO: use scala-redis for metadata, so it is cached in memory, but persisted to the filesystem
 */
private[impl] class FilesystemStorage(storageFolder: File) extends Storage with Metadata {
  require(storageFolder != null)
  require(storageFolder.isDirectory() || !storageFolder.exists())
  if (!storageFolder.exists())
    storageFolder.mkdirs()
  require(storageFolder.canWrite())
  

  implicit def handle2file(handle: Handle) = new File(storageFolder, handle.id.toString)
  
  class FileAccessor(override val handle: Handle) extends Accessor {
    
    def bytes = FileUtils.readFileToByteArray(handle)
    
    def bytes_=(value: Array[Byte]) {
      FileUtils.writeByteArrayToFile(handle, value)
    }
    
    def inputStream[A](code: InputStream => A): A = handleRequired(handle) {
      val inputStream = new FileInputStream(handle)
      try {
        code(inputStream)
      } finally {
        inputStream.close()
      }
    }
    
    def outputStream[A](code: OutputStream => A): A = handleRequired(handle) {
      val outputStream = new FileOutputStream(handle)
      try {
    	code(outputStream)
      } finally {
        outputStream.close()
      }
    }
  }

  override def create(path: String, name: String): Handle = throw new UnsupportedOperationException
  
  override def delete(handle: Handle) { throw new UnsupportedOperationException }
  
  override def apply(handle: Handle): Accessor = new FileAccessor(handle)
}
