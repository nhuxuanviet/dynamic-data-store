package com.company.dynamicdatastore.component.datastore;

import io.jmix.core.Metadata;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VirtualDataStoreManager {

    private final Map<String, VirtualDataStore> stores = new ConcurrentHashMap<>();
    private final Metadata metadata;

    public VirtualDataStoreManager(Metadata metadata) {
        this.metadata = metadata;
    }

    public VirtualDataStore createStore(String name) {
        return stores.computeIfAbsent(name, n -> new VirtualDataStore(n, metadata));
    }

    public VirtualDataStore getStore(String name) {
        return stores.get(name);
    }

    public Map<String, VirtualDataStore> getAllStores() {
        return stores;
    }
}
