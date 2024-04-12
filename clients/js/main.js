/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
 *
 * You may not use this file except in compliance with the Universal Permissive
 * License (UPL), Version 1.0 (the "License.")
 *
 * You may obtain a copy of the License at https: //opensource.org/licenses/UPL.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations
 * under the License.
*/

const coh = require('@oracle/coherence')
const uuid = require('uuid')

// aliases
const Processors = coh.Processors
const Filters = coh.Filters
const Aggregators = coh.Aggregators
const Session = coh.Session
const MapListener = coh.event.MapListener
const MapEventType = coh.event.MapEventType

// setup session to Coherence
const session = new Session()
const prices = session.getCache('Price')
const trades = session.getCache('Trade')

// currency formatter
const formatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
})

const args = process.argv.slice(2);

if (args.length < 1) {
    usage()
    process.exit(0)
}

let command = args[0]
let symbol = ""
let count = 0

if (args.length >= 2) {
    symbol = args[1]
}
if (args.length >= 3) {
    count = args[2]
}

setImmediate(async () => {
    if (command === "size") {
        console.log("Trade cache size = " + (await trades.size))
        console.log("Price cache size = " + (await prices.size))
    } else if (command === "monitor") {
        await monitor()
    } else if (command === "add-trades") {
        await addTrades(symbol, count)
    } else if (command === "stock-split") {
        await stockSplit(symbol, count)
    } else {
        usage()
    }

    process.exit(0)
})

// ----- helpers ------------------------------------------------------------

// add a number of trades for a symbol
async function addTrades(symbol, count) {
    if (count < 0) {
        console.log("Count must not be negative")
        return
    }

    let symbols = await prices.aggregate(Aggregators.distinct('symbol'))

    if (!symbols.includes(symbol, 0)) {
        console.log("Unable to find " + symbol + ", valid values are " + symbols)
        return
    }

    console.log(new Date().toISOString() + ": Adding %d random trades for %s...", count, symbol)

    // get the current price for the trade
    let currentPrice = await prices.get(symbol)

    let buffer = new Map()
    for (let i = 0; i < count; i++) {
        let trade = createTrade(symbol, Math.floor(Math.random() * 1000), currentPrice.price)
        buffer.set(trade.id, trade)
        if (i % 1000 === 0) {
            await trades.setAll(buffer)
            buffer.clear()
        }
    }
    
    if (buffer.size !== 0) {
        await trades.setAll(buffer)
    }

    let size = await trades.size
    console.log(new Date().toISOString() + ": Trades cache size is now " + size)
}

// split a stock using a given factor
async function stockSplit(symbol, factor) {
    if (factor < 0) {
        console.log("Factor must not be negative")
        return
    }

    let symbols = await prices.aggregate(Aggregators.distinct('symbol'))

    if (!symbols.includes(symbol, 0)) {
        console.log("Unable to find " + symbol + ", valid values are " + symbols)
        return
    }

    console.log(new Date().toISOString() + ": Splitting %s using factor of %d...", symbol, factor)

    // get the current price for the trade
    let currentPrice = await prices.get(symbol)

    // the process for the stock split is:
    // 1. Update each trade and multiply the quantity by thr factor
    // 2. Update each trade and divide the price by the factor (or multiply by 1/factor)
    // 3. Update the price cache for the symbol and divide the price by the factor (or multiply by 1/factor)

    let filter = Filters.equal("symbol", symbol)

    console.log(new Date().toISOString() + ": Updating quantity for " + symbol + " trades...")
    await trades.invokeAll(filter, Processors.multiply("quantity", factor))

    console.log(new Date().toISOString() + ": Updating price for " + symbol + " trades...")
    await trades.invokeAll(filter, Processors.multiply("price", 1 / factor))

    let newPrice = (currentPrice.price / factor)

    console.log(new Date().toISOString() + ": Updating price for " + symbol + " to " + formatter.format(newPrice))
    await prices.invoke(symbol, Processors.multiply("price", 1 / factor))
}

// monitor any price changes
async function monitor() {
    console.log("Listening for price changes. Press CTRL-C to finish.")
    const handler = (event) => {
        let oldPrice = event.oldValue.price;
        let newPrice = event.newValue.price;
        let change = newPrice - oldPrice;

        console.log("Price changed for " + event.key + ", new=" + formatter.format(newPrice) +
            ", old=" + formatter.format(oldPrice) + ", change=" + formatter.format(change))
    }
    const listener = new MapListener().on(MapEventType.UPDATE, handler)

    await prices.addMapListener(listener)
    await sleep(100_000_000)
}

function usage() {
    console.log("Usage: main.js command\n" +
        "The following commands are supported:\n" +
        "size        - display the cache sizes\n" +
        "monitor     - monitor prices\n" +
        "add-trades  - add random trades, specify symbol and count\n" +
        "stock-split - stock split, specify symbol and factor")
}

// create a Trade
function createTrade(symbol, qty, price) {
    const trade = {
        '@class': 'Trade',
        id: uuid.v4().toString(),
        symbol: symbol,
        quantity: qty,
        price: price
    }

    return trade
}

function sleep(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}


