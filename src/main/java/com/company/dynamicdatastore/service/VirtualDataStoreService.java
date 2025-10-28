package com.company.dynamicdatastore.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.company.dynamicdatastore.component.datastore.VirtualDataStore;
import com.company.dynamicdatastore.component.datastore.VirtualDataStoreManager;
import com.company.dynamicdatastore.component.entity.VirtualEntityDefinition;

@Service("dynamicdatastore_VirtualDataStoreService")
public class VirtualDataStoreService {

    private final VirtualDataStoreManager manager;
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.ConcurrentHashMap<String, VirtualEntityDefinition>> definitionsByStore = new java.util.concurrent.ConcurrentHashMap<>();

    public VirtualDataStoreService(VirtualDataStoreManager manager) {
        this.manager = manager;
    }

    // Store ops
    public String createStore(String storeName) {
        manager.createStore(storeName);
        return storeName;
    }

    // Definition ops
    public void registerEntityDefinition(String storeName, String entityName, Map<String, Object> properties) {
        manager.createStore(storeName);
        var defs = definitionsByStore.computeIfAbsent(storeName, s -> new java.util.concurrent.ConcurrentHashMap<>());
        var def = new VirtualEntityDefinition(entityName, storeName);
        defs.put(entityName, def);
    }

    // Entity ops
    public Object createEntity(String storeName, String entityName, Map<String, Object> data) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        // Sao chép sang HashMap để tránh UnsupportedOperationException khi data là unmodifiable
        java.util.HashMap<String, Object> entityData = new java.util.HashMap<>();
        if (data != null) entityData.putAll(data);
        if (entityData.get("id") == null)
            entityData.put("id", UUID.randomUUID());
        store.saveEntity(entityName, entityData);
        return entityData;
    }

    public int bulkCreateEntities(String storeName, String entityName, List<Map<String, Object>> rows) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        int count = 0;
        for (Map<String, Object> row : rows) {
            java.util.HashMap<String, Object> entityData = new java.util.HashMap<>();
            if (row != null) entityData.putAll(row);
            if (entityData.get("id") == null) {
                entityData.put("id", UUID.randomUUID());
            }
            store.saveEntity(entityName, entityData);
            count++;
        }
        return count;
    }

    public List<Map<String, Object>> query(String storeName, String entityName, Map<String, Object> filters) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        List<Object> list = store.loadAllEntities(entityName);
        return list.stream()
                .map(this::entityToMap)
                .filter(m -> matchesFilters(m, filters))
                .toList();
    }

    private boolean matchesFilters(Map<String, Object> entity, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return true;
        for (Map.Entry<String, Object> f : filters.entrySet()) {
            Object v = entity.get(f.getKey());
            if (f.getValue() == null) {
                if (v != null) return false;
            } else {
                if (v == null) return false;
                if (!String.valueOf(v).equalsIgnoreCase(String.valueOf(f.getValue()))) return false;
            }
        }
        return true;
    }

    public List<Object> loadAllEntities(String storeName, String entityName) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        return store.loadAllEntities(entityName);
    }

    public Object loadEntity(String storeName, String entityName, String id) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        return store.loadEntity(entityName, UUID.fromString(id));
    }

    public Object updateEntity(String storeName, String entityName, String id, Map<String, Object> data) {
        Object entity = loadEntity(storeName, entityName, id);
        @SuppressWarnings("unchecked")
        Map<String, Object> entityMap = (Map<String, Object>) entity;
        entityMap.putAll(data);
        VirtualDataStore store = manager.getStore(storeName);
        store.saveEntity(entityName, entityMap);
        return entityMap;
    }

    public Map<String, Integer> getStoreStatistics(String storeName) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        return store.getEntityCounts();
    }

    public Map<String, Object> entityToMap(Object entity) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) entity;
        return map;
    }

    public List<Map<String, Object>> entitiesToMaps(List<Object> entities) {
        return entities.stream().map(e -> entityToMap(e)).toList();
    }

    public Map<String, Object> getAllStores() {
        return manager.getAllStores().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of("name", e.getKey(), "entityCounts", e.getValue().getEntityCounts())));
    }

    public void deleteStore(String storeName) {
        manager.getAllStores().remove(storeName);
    }

    // Entity definition stubs
    public VirtualEntityDefinition getEntityDefinition(String storeName, String entityName) {
        var defs = definitionsByStore.get(storeName);
        if (defs == null)
            return null;
        return defs.get(entityName);
    }

    public java.util.List<VirtualEntityDefinition> getEntityDefinitions(String storeName) {
        var defs = definitionsByStore.get(storeName);
        if (defs == null)
            return java.util.List.of();
        return new java.util.ArrayList<>(defs.values());
    }

    public void deleteEntityDefinition(String storeName, String entityName) {
        var defs = definitionsByStore.get(storeName);
        if (defs != null)
            defs.remove(entityName);
    }

    public void deleteEntity(String storeName, String entityName, String id) {
        VirtualDataStore store = manager.getStore(storeName);
        if (store == null)
            throw new IllegalArgumentException("Store not found: " + storeName);
        store.deleteEntity(entityName, UUID.fromString(id));
    }
}
