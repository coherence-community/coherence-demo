# Polyglot Clients

## Overview

The Coherence Demonstration showcases how languages such as Go, Python and JavaScript can access Coherence
clusters using gRPC. This is achieved via configuring and starting a [gRPCProxy](https://docs.oracle.com/en/middleware/standalone/coherence/14.1.1.2206/develop-remote-clients/using-coherence-grpc-server.html).

> The Coherence gRPC proxy is the server-side implementation of the gRPC services defined within the Coherence gRPC module. The gRPC proxy uses standard gRPC Java libraries to provide Coherence APIs over gRPC.

This demonstration covers JavaScript, Python and Golang Coherence clients. If you would like ot know more about each one, then please see the relevant GitHub pages below:

* https://github.com/oracle/coherence-js-client
* https://github.com/oracle/coherence-py-client
* https://github.com/oracle/coherence-go-client


## Setup

The Coherence Demonstration already includes the grpc-proxy module as shown below:

```xml
 <dependency>
   <groupId>${coherence.group.id}</groupId>
   <artifactId>coherence-grpc-proxy</artifactId>
   <version>${coherence.version}</version>
 </dependency>
```

By default, the gRPC proxy listens on an ephemeral port, so we include the following in the `pom.xml` file to specify the default gRPC port of 1480.

```bash
-Dcoherence.grpc.server.port=1408
```

To ensure the `Trade` and `Price` objects are serialized into Java objects, we create the file `src/main/resources/META-INF/type-aliases.properties`
which contains the mappings determined by the `@class` JSON attribute on the objects.

```bash
Trade=com.oracle.coherence.demo.model.Trade
Price=com.oracle.coherence.demo.model.Price
```
           
## Running the Polyglot Demos

Each of the language demonstrations provide a simple command line interface, to showcase Coherence API's across gRPC.

They allow you to:
1. Display the size of the caches - `size`
2. Monitor for changes in stock prices via MapListeners - `monitor`
3. Add trades for a symbol - `add-trades symbol count`
4. Issue a stock-split via EntryProcessors - `stock-split symbol factor`

Once you have started the Coherence Demonstration, select from the follow to run the language demo of choice.

- [JavaScript](#javascript)
- [Python](#python)
- [Goang](#golang)

See [here](#examples) to see sample runs:   

## JavaScript

The code from this demonstration is available [here](js/main.js).

### Prerequisites

You must have the following installed:
* Node 18.15.x or later
* NPM 9.x or later

1. Open a terminal and change to the `clients/js` directory and issue the following to install the coherence-js-client:

   ```bash
   npm install
   ```
 
### Running the JavaScript demo 
 
```bash
node main.js
```

## Python

The code from this demonstration is available [here](py/main.py).

### Prerequisites

You must have Python 3.11.x or later installed.

1. Open a terminal and change to the `clients/py` directory and issue the following to install the coherence-py-client:

   ```bash
   python3 -m pip install coherence-client
   ```

### Running the Python demo

```bash
node main.py
```

## Golang

The code from this demonstration is available [here](go/main.go).

### Prerequisites

You must have the go 1.19 or above installed.

1. Open a terminal and change to the `clients/go` directory and issue the following to install the coherence-go-client:

   ```bash
   go get github.com/oracle/coherence-go-client@latest
   ```

2. Build and executable

   ```bash
   go build -o go-demo .
   ```
       
### Running the Go demo

```bash
./go-demo
```         
 
## Examples

Each of the programs have the same arguments, and you can pass as described above.      
Here we show an example of running the Go demo, but other languages take the same 
arguments and have similar output.

1. Display the usage by running `./go-demo`

   ```bash
   ./go-demo 

   Usage: main.go command
   The following commands are supported:
   size        - display the cache sizes
   monitor     - monitor prices
   add-trades  - add random trades, specify symbol and count
   stock-split - stock split, specify symbol and factor
   ```
   
2. Display the cache sizes by running `./go-demo size`

   ```bash
   ./go-demo size
   2024/04/10 13:07:23 session: 68632e4d-70c4-43f4-8532-95ab5f583177 connected to address localhost:1408

   2024/04/10 13:07:24 Trade cache size = 200005
   2024/04/10 13:07:24 Price cache size = 6

   2024/04/10 13:07:25 closed session 68632e4d-70c4-43f4-8532-95ab5f583177
   ```

3. Add trades for a symbol by running `./go-demo add-trades ORCL 100000`
 
   ```bash
   ./go-demo add-trades ORCL 100000
   2024/04/10 13:12:11 session: c070b210-3494-4bf2-962e-24b526c17d56 connected to address localhost:1408

   2024/04/10 13:12:11 Adding 100000 random trades for ORCL...
   2024/04/10 13:12:11 Trades cache size is now 300005

   2024/04/10 13:12:14 closed session c070b210-3494-4bf2-962e-24b526c17d56
   ```

4. Issue a stock split for `ORCL` stock using 2:1 factor by running `./go-demo stock-split ORCL 2`
 
   > Note: Ensure you have turned off price updates from the UI.
   
   ```bash
   ./go-demo stock-split ORCL 2
   2024/04/10 13:16:16 session: de1599ef-56e9-44e3-bbbf-01d7f82bbd01 connected to address localhost:1408

   2024/04/10 13:16:18 Updated quantity for 16747 trades
   2024/04/10 13:16:18 Updated price for 16747 trades
   2024/04/10 13:16:18 Updated price for ORCL from $20.00 to $10.00

   2024/04/10 13:16:18 closed session de1599ef-56e9-44e3-bbbf-01d7f82bbd01
   ```

6. Monitor price updates by running `./go-demo monitor`

   ```bash
   ./go-demo monitor
   2024/04/10 13:09:14 session: 92cf9633-3e0b-429a-bc19-93863bf3bea2 connected to address localhost:1408

   Listening for price changes. Press CTRL-C to finish.
   ```                                                 
   
   Open the demonstration page at http://127.0.0.1:8080/application/index.html and click the `Real-Time Price Updates` checkbox.
   You will see the updates displayed similar to below:

   ```bash
   2024/04/10 13:11:01 Price changed for ORCL, new=$5.00, old=$5.23, change=$0.23
   2024/04/10 13:11:06 Price changed for NFLX, new=$20.00, old=$19.04, change=$-0.96
   2024/04/10 13:11:11 Price changed for NFLX, new=$19.04, old=$19.00, change=$-0.04
   2024/04/10 13:11:16 Price changed for MSFT, new=$20.08, old=$19.88, change=$-0.20
   ```
   
   Press `CTRL-C` to exit.
