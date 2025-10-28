package com.company.dynamicdatastore.component.registy;

import com.company.dynamicdatastore.component.datastore.VirtualDataStore;
import io.jmix.core.DataStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

@Component("dynamicdatastore_DataStoreRegistry")
public class DataStoreRegistry {

    private final Map<String, DataStore> stores = new ConcurrentHashMap<>();
    private final Map<String, VirtualDataStore> virtualStores = new ConcurrentHashMap<>();

    public void register(DataStore store) {
        stores.put(store.getName(), store);
        System.out.println("✅ Registered datastore: " + store.getName());

        // Nếu là VirtualDataStore, đăng ký vào virtualStores
        if (store instanceof VirtualDataStore) {
            virtualStores.put(store.getName(), (VirtualDataStore) store);
            System.out.println("✅ Registered virtual datastore: " + store.getName());
        }
    }

    public void unregister(String storeName) {
        DataStore removed = stores.remove(storeName);
        VirtualDataStore virtualRemoved = virtualStores.remove(storeName);

        if (removed != null) {
            System.out.println("❌ Unregistered datastore: " + storeName);
        }

        if (virtualRemoved != null) {
            System.out.println("❌ Unregistered virtual datastore: " + storeName);
        }
    }

    public DataStore get(String storeName) {
        return stores.get(storeName);
    }

    public VirtualDataStore getVirtualStore(String storeName) {
        return virtualStores.get(storeName);
    }

    public boolean contains(String storeName) {
        return stores.containsKey(storeName);
    }

    public boolean containsVirtualStore(String storeName) {
        return virtualStores.containsKey(storeName);
    }

    public Map<String, DataStore> getAll() {
        return stores;
    }

    public Map<String, VirtualDataStore> getAllVirtualStores() {
        return virtualStores;
    }

    public List<String> getVirtualStoreNames() {
        return new ArrayList<>(virtualStores.keySet());
    }

    public List<String> getAllStoreNames() {
        return new ArrayList<>(stores.keySet());
    }

    /**
     * Lấy thống kê tổng quan về các stores
     */
    public Map<String, Object> getRegistryStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalStores", stores.size());
        stats.put("virtualStores", virtualStores.size());
        stats.put("regularStores", stores.size() - virtualStores.size());

        Map<String, Object> virtualStoreStats = new ConcurrentHashMap<>();
        for (Map.Entry<String, VirtualDataStore> entry : virtualStores.entrySet()) {
            String storeName = entry.getKey();
            VirtualDataStore store = entry.getValue();

            Map<String, Object> storeStats = new ConcurrentHashMap<>();
            storeStats.put("entityCounts", store.getEntityCounts());
            storeStats.put("entityDefinitions", store.getEntityDefinitions().keySet());

            virtualStoreStats.put(storeName, storeStats);
        }
        stats.put("virtualStoreDetails", virtualStoreStats);

        return stats;
    }
}
