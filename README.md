JMXPROXY
========

JMXProxy exposes all available mbean attributes available on a given JVM via simple HTTP request.  The results are easily-parsable `json` encoding.  The server component is built using [Dropwizard](http://dropwizard.codahale.com/).


Compiling
---------

The build is simple `maven2` invocation.

    $ mvn clean package


Execution
---------

The result is a single `jar` file that contains all the bits necessary to start and run the server.

    $ java -jar target/jmxproxy-2.0.1-SNAPSHOT.jar

A more complex example that enables `JMX` and limits heap may look something like this:

    $ java -Xmx100m -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1123 -jar target/jmxproxy-2.0.1-SNAPSHOT.jar


Configuration
-------------

Configuration is handled entirely by [Dropwizard](http://dropwizard.codahale.com/manual/core/#configuration-defaults).  For example, to change the server listen port via command-line add this parameter

    $ -Ddw.http.port=9090

For more complex configuration settings, create a `yaml` file and point to it at startup by adding it to the command-line as the last parameter.
