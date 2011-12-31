#!/bin/sh
if [ ! -d "$REBEL_HOME" ]; then 
  echo "REBEL_HOME env. var must be set"
  exit 1 
fi
MAVEN_OPTS="-noverify -javaagent:$REBEL_HOME/jrebel.jar" mvn compile exec:java
