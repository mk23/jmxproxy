JMXPROXY
========

JMXProxy exposes all available mbean attributes available on a given JVM via simple HTTP request.  The results are in easily-parsable json format.  The server component is built using [Dropwizard](http://dropwizard.codahale.com/).


Compiling
---------

The build is simple maven2 invocation.

    $ mvn clean package


Configuration
-------------

Configuration is handled entirely by [Dropwizard](http://dropwizard.codahale.com/manual/core/#configuration-defaults).  For example, to change the server listen port via command-line add this parameter:

    $ -Ddw.http.port=9090

For more complex configuration settings, create a yaml file and point to it at startup by adding it to the command-line as the last parameter.

    $ java -jar target/jmxproxy-2.1.1-SNAPSHOT.jar server config.yaml


Execution
---------

The result is a single jar file that contains all the bits necessary to start and run the server.

    $ java -jar target/jmxproxy-2.1.1-SNAPSHOT.jar server

A more complex example that enables the [JMX Agent](http://docs.oracle.com/javase/7/docs/technotes/guides/management/agent.html) and limits heap may look something like this:

    $ java -Xmx100m -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1123 -jar target/jmxproxy-2.1.1-SNAPSHOT.jar server


Usage
-----

The server responds to a standard HTTP GET request, where the URI specifies the destination JMX agent to contact.  For example, using `curl`:

    $ curl http://localhost:8080/localhost:1123
    {"java.lang:type=MemoryPool,name=PS Old Gen": ...

This standard request returns dictionary where keys are the full mbean path and the value is a dictionary of all attribute key/values for that mbean.  Alternatively, a user can pass a boolean query parameter, `domains`, to group mbeans together.

    $ curl 'http://localhost:8080/localhost:1123?domains=true'
    {"java.lang":{"java.lang:type=MemoryPool,name=PS Old Gen": ...


Limitations
-----------

* Agent authentication is currently not supported.  Remote JVM must be started with `-Dcom.sun.management.jmxremote.authentication=false`.
* SSL agent connections are currently not supported.  Remote JVM must be started with `-Dcom.sun.management.jmxremote.ssl=false`.

These may be implemented in future versions.


Example Clients
---------------

*   `scripts/cacti/ss_jmxproxy.php <host:port> [jmxproxy-host:port]`:  
    [Cacti](http://www.cacti.net) ScriptServer plugin.  For example, to request basic stats from the JVM running `jmxproxy` itself:

        $ php -q ss_jmxproxy.php localhost:1123 localhost:8080
        thread_count:35 thread_peak:35 memory_heap_used:6763352 memory_heap_max:101384192 gc_count:19 classes_loaded:3679 classes_total:3679 classes_unloaded:0

    This plugin allows easy extensions by creating another PHP file that includes `ss_jmxproxy.php`, sets up an array of desired beans to fetch, and passes it to the `ss_jmxproxy()` function.  One such extension, `ss_hadoop.php`, exists to demonstrate this behavior and usage:

        $ php -q ss_hadoop.php datanode001:8003 datanode localhost:8080
        thread_count:165 thread_peak:593 memory_heap_used:158213848 memory_heap_max:1908932608 gc_count:27465 classes_loaded:2717 classes_total:2775 classes_unloaded:58 ds_capacity:2869079572480 ds_remaining:1665482752 ds_used:2750974428881 ds_failed:0 blocks_read:1230 blocks_removed:167 blocks_replicated:0 blocks_verified:28 blocks_written:133 bytes_read:222298772 bytes_written:8175757632 reads_from_local_client:1165 writes_from_local_client:36 reads_from_remote_client:65 writes_from_remote_client:97 ops_block_copy:0 ops_block_read:1230 ops_block_write:133 ops_block_replace:0 ops_block_checksum:0 ops_block_reports:0 ops_heartbeat:100

*   `scripts/graphite/jmxproxy.py --service-port <port> [other options]`:  
    [Graphite](http://graphite.wikidot.com) poller script, see `--help` for more options.  For example to request basic stats from the JVM running `jmxproxy` itself:

        $ scripts/graphite/jmxproxy.py --service-port 1123 --service-host localhost --jmxproxy-host localhost --jmxproxy-port 8080 -n # dry-run output
        localhost.jvm.classes_loaded 3804 1363021890
        localhost.jvm.classes_total 3917 1363021890
        localhost.jvm.classes_unloaded 113 1363021890
        localhost.jvm.gc_count 43 1363021890
        localhost.jvm.memory_heap_max 101384192 1363021890
        localhost.jvm.memory_heap_used 5893816 1363021890
        localhost.jvm.thread_count 36 1363021890
        localhost.jvm.thread_peak 36 1363021890

    This script allows easy extensions by creating another script that imports `jmxproxy`, sets up a dictionary of desired beans, and passes it to the `jmxproxy.main()` function.  One such extension, `hadoopy.py`, exists to demonstrate this behavior and usage:

        $ scripts/graphite/hadoop.py --service-port 8003 --service-host datanode001 --jmxproxy-host localhost --jmxproxy-port 8080 --service-name datanode -n # dry-run output
        datanode001.datanode.blocks_read 1839 1363022233
        datanode001.datanode.blocks_removed 1584 1363022233
        datanode001.datanode.blocks_replicated 0 1363022233
        datanode001.datanode.blocks_verified 25 1363022233
        datanode001.datanode.blocks_written 192 1363022233
        datanode001.datanode.bytes_read 6320489641 1363022233
        datanode001.datanode.bytes_written 9864394003 1363022233
        datanode001.datanode.ds_capacity 2869079572480 1363022233
        datanode001.datanode.ds_failed 0 1363022233
        datanode001.datanode.ds_remaining 145051308032 1363022233
        datanode001.datanode.ds_used 2607723919940 1363022233
        datanode001.datanode.jvm.classes_loaded 2717 1363022233
        datanode001.datanode.jvm.classes_total 2805 1363022233
        datanode001.datanode.jvm.classes_unloaded 88 1363022233
        datanode001.datanode.jvm.gc_count 32970 1363022233
        datanode001.datanode.jvm.memory_heap_max 1908932608 1363022233
        datanode001.datanode.jvm.memory_heap_used 146242304 1363022233
        datanode001.datanode.jvm.thread_count 214 1363022233
        datanode001.datanode.jvm.thread_peak 593 1363022233
        datanode001.datanode.ops_block_checksum 0 1363022233
        datanode001.datanode.ops_block_copy 187 1363022233
        datanode001.datanode.ops_block_read 1652 1363022233
        datanode001.datanode.ops_block_replace 0 1363022233
        datanode001.datanode.ops_block_reports 0 1363022233
        datanode001.datanode.ops_block_write 192 1363022233
        datanode001.datanode.ops_heartbeat 100 1363022233
        datanode001.datanode.reads_from_local_client 1370 1363022233
        datanode001.datanode.reads_from_remote_client 282 1363022233
        datanode001.datanode.writes_from_local_client 50 1363022233
        datanode001.datanode.writes_from_remote_client 142 1363022233


License
-------
TBD
