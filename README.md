# TIO
Demonstrates creating a Toy IO effect system (TIO) from ground up.
The architecture and API loosely follows [ZIO](https://github.com/zio/zio), but for simplicitly omits typed errors and environment.

You can follow the progression of building up TIO using tags:
1. [Creating and combining effects](https://github.com/dkarlinsky/tio/tree/iteration1/src/main/scala/tio)
1. [Failing and Recovering from Errors](https://github.com/dkarlinsky/tio/tree/iteration2/src/main/scala/tio)
1. [Asynchrony with Callbacks](https://github.com/dkarlinsky/tio/tree/iteration3.1/src/main/scala/tio)
1. [Queuing work](https://github.com/dkarlinsky/tio/tree/iteration3.2/src/main/scala/tio)
1. [Concurrency with Fibers](https://github.com/dkarlinsky/tio/tree/iteration4/src/main/scala/tio)
