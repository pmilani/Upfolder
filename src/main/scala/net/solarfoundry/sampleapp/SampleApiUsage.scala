package net.solarfoundry.sampleapp

import net.solarfoundry.upfolder.impl.CreateStorage
import java.io.File

class SampleApiUsage {

  def demoCreation() {
    CreateStorage.inMemory()
    
    CreateStorage.onFilesystem(new File("/tmp/upfolder.data"))
    
    CreateStorage.eventGenerating.inMemory()
    
    CreateStorage.eventGenerating.onFilesystem(new File("/tmp/upfolder.eventual.data"))
    
    CreateStorage.eventGenerating.andLogging.inMemory()

    CreateStorage.eventGenerating.andLogging.onFilesystem(new File("/tmp/upfolder.eventual.data"))

    CreateStorage.eventGenerating.andCollecting.inMemory()
    
    CreateStorage.eventGenerating.andCollecting.onFilesystem(new File("/tmp/upfolder.eventual.data"))
  }
}