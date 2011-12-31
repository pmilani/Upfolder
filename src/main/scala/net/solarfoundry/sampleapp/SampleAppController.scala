package net.solarfoundry.sampleapp

import org.scalatra._

class SampleAppController extends ScalatraServlet {
  
  notFound {
    <html>
      <head>
        <link rel="stylesheet" href="http://twitter.github.com/bootstrap/1.4.0/bootstrap.min.css"/>
      </head>
      <body>
        <div class="container">
          <h3>Not found</h3>
          The location you're trying to access seems to be invalid.
        </div>
      </body>
    </html>
  }
  
  get("/") {
    <html>
      <head>
        <link rel="stylesheet" href="http://twitter.github.com/bootstrap/1.4.0/bootstrap.min.css"/>
      </head>
      <body>
        <div class="container">
        </div>
      </body>
    </html>
  }
  
}