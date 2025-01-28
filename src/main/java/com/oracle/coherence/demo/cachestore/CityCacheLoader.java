package com.oracle.coherence.demo.cachestore;

import com.tangosol.net.cache.CacheLoader;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class CityCacheLoader implements CacheLoader<String, String> {
    @Override
    public String load(String o) {

        if (o == null)
        {
            return null;
        }

        String value = null;

        switch (o)
        {
            case "ny" : value = "New York"; break;
            case "dal" : value = "Dallas"; break;
            case "aus" : value = "Austin"; break;
            case "la" : value = "Los Angeles"; break;
        }

        return value;
    }

    @Override
    public Map<String, String> loadAll(Collection<? extends String> colKeys) {

        if (colKeys.isEmpty())
        {
            return Map.of();
        }

        return colKeys.stream()
                      .collect(toMap(Function.identity(), this::load));
    }

}
