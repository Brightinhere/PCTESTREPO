### IndexBuilder_RMI ###

Example of using Java Remote Method Invokation by a number of worker processes
who do some work and share the results with a remote service.

This example also shows how to launch of worker processes on the same local host automatically from the start of a single main.

Just run IndexBuilderMain

That will:
1. Instantiate the master service
2. Create an RMI registry and register the master service
3. launch the client workers
4. each worker identifies the master service from the registry
5. workers work and share
6. the main waits until are workers are finished and shows the result.

Each worker proces generates a large number of small random words
and builds a map counting how often each word was generated.
It then builds an index aggregating the counts by first letter of these words.
So that index will hold max 26 entries.

Then it shares the index with the masterServiceObject by remote method invokation.
That masterServiceObject aggregates the worker contributions into a concurrentHashMap.
Notice that the remote methods are thread safe without need for the synchronised keyword on the methods themselves.
(because of use of concurrent data structures)


