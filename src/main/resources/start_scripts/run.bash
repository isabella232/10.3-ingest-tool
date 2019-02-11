#!/bin/bash
URL="http://localhost:8080"

# To change the default port add the following parameter in the starting line: --server.port=9090, so the command will take the following form:
# java -jar demo-0.0.1-SNAPSHOT.jar --server.port=9090

run_server(){
	cd lib
	java -jar demo-0.0.1-SNAPSHOT.jar --server.port=9090
}

pop_browser(){
	if which xdg-open > /dev/null
	then
	  xdg-open $1
	elif which gnome-open > /dev/null
	then
	  gnome-open $1
	fi
}

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." 1>&2
  echo "Please install java and try again." 1>&2
  pop_browser "http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html"
  exit 1
fi

if [ ! -x "$BASH" ] ;then
  echo "Please run with 'bash run.bash'."
  exit 1
fi

run_server & p1=$! & sleep 5; pop_browser $URL & wait $p1


