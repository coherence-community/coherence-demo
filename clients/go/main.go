/*
* Copyright (c) 2024, 2025 Oracle and/or its affiliates.
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

package main

import (
	"context"
	"errors"
	"fmt"
	"github.com/google/uuid"
	"github.com/oracle/coherence-go-client/v2/coherence"
	"github.com/oracle/coherence-go-client/v2/coherence/extractors"
	"github.com/oracle/coherence-go-client/v2/coherence/filters"
	"github.com/oracle/coherence-go-client/v2/coherence/processors"
	"log"
	"math/rand"
	"os"
	"strconv"
	"time"
)

type Trade struct {
	Class    string  `json:"@class"`
	ID       string  `json:"id"`
	Symbol   string  `json:"symbol"`
	Quantity int     `json:"quantity"`
	Price    float32 `json:"price"`
}

type Price struct {
	Class  string  `json:"@class"`
	Symbol string  `json:"symbol"`
	Price  float32 `json:"price"`
}

var (
	ctx = context.Background()
)

func main() {
	var (
		trades  coherence.NamedCache[string, Trade]
		prices  coherence.NamedCache[string, Price]
		options []string
		argsLen = len(os.Args)
	)

	if argsLen < 2 {
		usage()
		return
	}

	command := os.Args[1]
	if argsLen > 2 {
		options = os.Args[2:]
	}

	// create a new Session
	session, err := coherence.NewSession(ctx, coherence.WithPlainText(), coherence.WithRequestTimeout(time.Duration(120)*time.Second))
	if err != nil {
		panic(err)
	}
	defer session.Close()

	trades, err = coherence.GetNamedCache[string, Trade](session, "Trade")
	if err != nil {
		panic(err)
	}

	prices, err = coherence.GetNamedCache[string, Price](session, "Price")
	if err != nil {
		panic(err)
	}

	fmt.Println()

	switch command {
	case "size":
		err = displaySize(trades, prices)
	case "monitor":
		err = listenPrices(prices)
	case "add-trades":
		err = addTrades(trades, prices, options...)
	case "stock-split":
		err = stockSplit(trades, prices, options...)
	default:
		err = fmt.Errorf("invalid option %s\n", command)

	}

	if err != nil {
		fmt.Println(err)
	}
}

func displaySize(trades coherence.NamedCache[string, Trade], prices coherence.NamedCache[string, Price]) error {
	size, err := trades.Size(ctx)
	if err != nil {
		return err
	}
	fmt.Printf("Trade cache size = %d\n", size)

	size, err = prices.Size(ctx)
	if err != nil {
		return err
	}
	fmt.Printf("Price cache size = %d\n\n", size)
	return nil
}

func listenPrices(prices coherence.NamedCache[string, Price]) error {
	fmt.Println("Listening for price changes. Press CTRL-C to finish.")
	fmt.Println()

	// Create a listener and add to the cache
	listener := coherence.NewMapListener[string, Price]().OnUpdated(func(e coherence.MapEvent[string, Price]) {
		key, err := e.Key()
		if err != nil {
			panic(err)
		}
		newValue, err := e.NewValue()
		if err != nil {
			panic(err)
		}
		oldValue, err := e.OldValue()
		if err != nil {
			panic(err)
		}

		newPrice := newValue.Price
		oldPrice := oldValue.Price
		change := newPrice - oldPrice
		log.Printf("Price changed for %s, new=$%3.2f, old=$%3.2f, change=$%3.2f\n", *key, oldPrice, newPrice, change)
	})

	if err := prices.AddListener(ctx, listener); err != nil {
		return err
	}

	select {}
}

func addTrades(trades coherence.NamedCache[string, Trade], prices coherence.NamedCache[string, Price], options ...string) error {
	if len(options) != 2 {
		return fmt.Errorf("you must specify a symbol and count")
	}

	symbol := options[0]
	count, err := strconv.Atoi(options[1])
	if err != nil {
		return fmt.Errorf("invalid value for count of %v", options[1])
	}

	if count < 0 {
		return errors.New("count cannot be negative")
	}

	symbols, err := getSymbols(prices)
	if err != nil {
		return err
	}

	if !isSymbolValid(symbol, symbols) {
		return fmt.Errorf("unable to find symbol %s, valid values are %v\n", symbol, symbols)
	}

	// get the price for the symbol
	currentPrice, err := prices.Get(ctx, symbol)
	if err != nil {
		return err
	}

	// add using efficient PuAll
	buffer := make(map[string]Trade, 0)

	log.Printf("Adding %d random trades for %s...\n", count, symbol)

	for i := 0; i < count; i++ {
		trade := newTrade(symbol, rand.Intn(1000)+1, currentPrice.Price)
		buffer[trade.ID] = trade
		if i%1000 == 0 {
			err = trades.PutAll(ctx, buffer)
			if err != nil {
				return err
			}
			buffer = make(map[string]Trade, 0)
		}
	}

	// if anything left in buffer save to Coherence
	if len(buffer) > 0 {
		err = trades.PutAll(ctx, buffer)
		if err != nil {
			return err
		}
	}

	size, err := trades.Size(ctx)
	if err == nil {
		log.Printf("Trades cache size is now %d\n", size)
		fmt.Println()
	}

	return nil
}

func stockSplit(trades coherence.NamedCache[string, Trade], prices coherence.NamedCache[string, Price], options ...string) error {
	if len(options) != 2 {
		return fmt.Errorf("you must specify a symbol and factor")
	}

	symbol := options[0]
	factor, err := strconv.Atoi(options[1])
	if err != nil {
		return fmt.Errorf("invalid value for factor of %v", options[1])
	}

	if factor < 1 || factor > 10 {
		return errors.New("factor must be between 1 and 10")
	}

	symbols, err := getSymbols(prices)
	if err != nil {
		return err
	}

	if !isSymbolValid(symbol, symbols) {
		return fmt.Errorf("unable to find symbol %s, valid values are %v\n", symbol, symbols)
	}

	// get the price for the symbol
	currentPrice, err := prices.Get(ctx, symbol)
	if err != nil {
		return err
	}

	// the process for the stock split is:
	// 1. Update each trade and multiply the quantity by thr factor
	// 2. Update each trade and divide the price by the factor (or multiply by 1/factor)
	// 3. Update the price cache for the symbol and divide the price by the factor (or multiply by 1/factor)

	symbolExtractor := extractors.Extract[string]("symbol")

	ch := coherence.InvokeAllFilter[string, Trade, int64](ctx, trades, filters.Equal(symbolExtractor, symbol),
		processors.Multiply("quantity", factor))

	count := 0
	for v := range ch {
		if v.Err != nil {
			return v.Err
		}
		count++
	}

	log.Printf("Updated quantity for %d trades", count)

	count = 0
	ch2 := coherence.InvokeAllFilter[string, Trade, float64](ctx, trades, filters.Equal(symbolExtractor, symbol),
		processors.Multiply("price", float32(1)/float32(factor)))

	for v := range ch2 {
		if v.Err != nil {
			return v.Err
		}
		count++
	}

	log.Printf("Updated price for %d trades", count)

	_, err = coherence.Invoke[string, Price, float32](ctx, prices, symbol, processors.Multiply("price", float32(1)/float32(factor)))

	log.Printf("Updated price for %s from $%3.2f to $%3.2f\n\n", symbol, currentPrice.Price, currentPrice.Price/float32(factor))
	// update the price cache
	return nil
}

func getSymbols(prices coherence.NamedCache[string, Price]) ([]string, error) {
	symbols := make([]string, 0)
	for ch := range prices.KeySet(ctx) {
		if ch.Err != nil {
			return symbols, ch.Err
		}
		symbols = append(symbols, ch.Key)
	}

	return symbols, nil
}

func isSymbolValid(symbol string, symbols []string) bool {
	found := false
	for _, v := range symbols {
		if v == symbol {
			found = true
			break
		}
	}

	return found
}

func usage() {
	fmt.Println("\nUsage: main.go command")
	fmt.Println("The following commands are supported:")
	fmt.Println("size        - display the cache sizes")
	fmt.Println("monitor     - monitor prices")
	fmt.Println("add-trades  - add random trades, specify symbol and count")
	fmt.Println("stock-split - stock split, specify symbol and factor")
	os.Exit(1)
}

func newTrade(symbol string, qty int, price float32) Trade {
	return Trade{Class: "Trade",
		ID:       uuid.New().String(),
		Symbol:   symbol,
		Quantity: qty,
		Price:    price,
	}
}
