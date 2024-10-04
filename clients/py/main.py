#
# Copyright (c) 2024 Oracle and/or its affiliates.
#
# You may not use this file except in compliance with the Universal Permissive
# License (UPL), Version 1.0 (the "License.")
#
# You may obtain a copy of the License at https: //opensource.org/licenses/UPL.
#
# Unless required by applicable law or agreed to in writing, software distributed
# under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
# CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and limitations
# under the License.
#
import uuid
import datetime
import random
import asyncio
import sys
from typing import List
from dataclasses import dataclass

from coherence import Filters, Aggregators, NamedCache, Session, Processors
from coherence.event import MapListener
from coherence import serialization


@dataclass
@serialization.proxy("Price")
class Price:
    symbol: str
    price: float


@dataclass
@serialization.proxy("Trade")
class Trade:
    id: str
    symbol: str
    quantity: int
    price: float


session: Session
prices: NamedCache[str, Price]
trades: NamedCache[str, Trade]


async def init_coherence() -> None:
    """
    Initialize Coherence.

    :return: None
    """
    global session
    global prices
    global trades

    session = await Session.create()
    prices = await session.get_cache("Price")
    trades = await session.get_cache("Trade")


async def run_demo() -> None:
    """
    Run the command line demo.

    :return: None
    """
    global session

    try:
        await init_coherence()

        if len(sys.argv) < 2:
            usage()
        else:
            command = sys.argv[1]
            symbol: str = ""
            count: int = 0

            if len(sys.argv) >= 3:
                symbol = sys.argv[2]
            if len(sys.argv) >= 4:
                count = int(sys.argv[3])

            if command == "size":
                await display_cache_size()
            elif command == "monitor":
                await monitor_prices()
            elif command == "add-trades":
                await add_trades(symbol, count)
            elif command == "stock-split":
                await stock_split(symbol, count)

    finally:
        await session.close()


async def display_cache_size() -> None:
    """
    Displays the size for both the Trade and Price caches.

    :return: None
    """
    global prices
    global trades

    tradesize = await trades.size()
    pricesize = await prices.size()

    print(f"Trade cache size: {tradesize}")
    print(f"Price cache size: {pricesize}")


async def monitor_prices() -> None:
    """
    Monitors the Price cache for any changes and displays them.

    :return: None
    """
    global prices

    listener: MapListener[str, Price] = MapListener()
    listener.on_updated(lambda e: handle_event(e))
    await prices.add_map_listener(listener)

    print("Listening for price changes. Press CTRL-C to finish.")
    await asyncio.sleep(10000)


def handle_event(e) -> None:
    """
    Event handler to display the event details

    :return: None
    """
    symbol = e.key
    old_price = e.old.price
    new_price = e.new.price
    change = new_price - old_price

    print(
        f"Price changed for {symbol}, new=${new_price:.2f}, old=${old_price:.2f}, change=${change:.2f}")


async def add_trades(symbol: str, count: int) -> None:
    """
    Add trades for a symbol.

    :param symbol the symbol to add trades to
    :param count the number of trades to add
    :return: None
    """
    global prices
    global trades

    if count <= 0:
        print("count must be supplied and be positive")
        return

    symbols: List[str] = await prices.aggregate(Aggregators.distinct("symbol"))

    if symbol in symbols:
        current_price: Price = await prices.get(symbol)

        buffer: dict[str, Trade] = {}
        print()

        print(f"{get_time()}: Adding {count} random trades for {symbol}")

        for i in range(0, count):
            trade_id = str(uuid.uuid1())
            new_trade: Trade = Trade(trade_id, symbol, random.randint(1, 1000), current_price.price)
            buffer[trade_id] = new_trade
            if i % 1000 == 0:
                await trades.put_all(buffer)
                buffer.clear()

        # Write anything left
        if len(buffer) != 0:
            await trades.put_all(buffer)

        size = await trades.size()
        print(f"{get_time()}: Size of Trade cache is now {size}")
    else:
        print(f"Unable to find {symbol}, valid symbols are {symbols}")


async def stock_split(symbol: str, factor: int) -> None:
    """
    Do a stock split.

    :param symbol the symbol to split
    :param factor the factor to use for the split, e.g. 2 = 2 to 1
    :return: None
    """
    global prices
    global trades

    if factor <= 0 or factor > 10:
        print("factor must be supplied and be positive and less than 10")
        return

    symbols: List[str] = await prices.aggregate(Aggregators.distinct("symbol"))

    if symbol in symbols:
        current_price: Price = await prices.get(symbol)

        # the process for the stock split is:
        # 1. Update each trade and multiply the quantity by thr factor
        # 2. Update each trade and divide the price by the factor (or multiply by 1/factor)
        # 3. Update the price cache for the symbol and divide the price by the factor (or multiply by 1/factor)

        print()
        print(f"{get_time()}: Splitting {symbol} using factor of {factor}")

        print(f"{get_time()}: Update quantity for {symbol}")
        async for _ in trades.invoke_all(Processors.multiply("quantity", factor), None, Filters.equals("symbol", symbol)):
            break  # ignore

        print(f"{get_time()}: Update price for {symbol}")
        async for _ in trades.invoke_all(Processors.multiply("price", 1 / factor), None, Filters.equals("symbol", symbol)):
            break  # ignore

        await prices.invoke(symbol, Processors.multiply("price", 1 / factor))

        new_price = (current_price.price / factor)

        print(f"{get_time()}: Updating price for {symbol} to ${new_price:.2f}")
    else:
        print(f"Unable to find {symbol}, valid symbols are {symbols}")


def get_time() -> str:
    return datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")


def usage():
    print("Usage: main.py command")
    print("The following commands are supported:")
    print("size        - display the cache sizes ")
    print("monitor     - monitor prices")
    print("add-trades  - add random trades, specify symbol and count")
    print("stock-split - stock split, specify symbol and factor")


asyncio.run(run_demo())
