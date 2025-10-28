package com.company.dynamicdatastore.component.datastore;

import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.datastore.AbstractDataStore;
import io.jmix.core.entity.EntityValues;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualDataStore extends AbstractDataStore {

    private final Map<String, List<Object>> storeData = new ConcurrentHashMap<>();
    private final String storeName;
    private final Metadata metadata;

    public VirtualDataStore(String storeName, Metadata metadata) {
        this.storeName = storeName;
        this.metadata = metadata;
    }

    @Override
    public String getName() {
        return storeName;
    }

    @Override
    public void setName(String name) {
        // no-op
    }

    // --- Load operations ---
    @Override
    protected Object loadOne(LoadContext<?> context) {
        List<Object> list = storeData.getOrDefault(context.getEntityMetaClass().getName(), Collections.emptyList());
        return list.stream()
                .filter(e -> Objects.equals(EntityValues.getId(e), context.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    protected List<Object> loadAll(LoadContext<?> context) {
        return new ArrayList<>(storeData.getOrDefault(context.getEntityMetaClass().getName(), Collections.emptyList()));
    }

    @Override
    protected long countAll(LoadContext<?> context) {
        return storeData.getOrDefault(context.getEntityMetaClass().getName(), Collections.emptyList()).size();
    }

    // --- Save/Delete operations ---
    @Override
    protected Set<Object> saveAll(SaveContext context) {
        Set<Object> result = new HashSet<>();
        for (Object entity : context.getEntitiesToSave()) {
            storeData.computeIfAbsent(metadata.getClass(entity).getName(), k -> new ArrayList<>()).add(entity);
            result.add(entity);
        }
        return result;
    }

    @Override
    protected Set<Object> deleteAll(SaveContext context) {
        Set<Object> result = new HashSet<>();
        for (Object entity : context.getEntitiesToRemove()) {
            List<Object> list = storeData.get(metadata.getClass(entity).getName());
            if (list != null) {
                list.removeIf(e -> Objects.equals(EntityValues.getId(e), EntityValues.getId(entity)));
            }
            result.add(entity);
        }
        return result;
    }

    // --- Values API (KeyValueEntity) ---
    @Override
    protected List<Object> loadAllValues(ValueLoadContext context) {
        return new ArrayList<>(storeData.getOrDefault(context.getQuery().getQueryString(), Collections.emptyList()));
    }

    @Override
    protected long countAllValues(ValueLoadContext context) {
        return storeData.getOrDefault(context.getQuery().getQueryString(), Collections.emptyList()).size();
    }

    // --- Transaction mocks ---
    @Override
    protected Object beginLoadTransaction(boolean joinTransaction) {
        return new Object();
    }

    @Override
    protected Object beginSaveTransaction(boolean joinTransaction) {
        return new Object();
    }

    @Override
    protected void commitTransaction(Object transaction) {
        // no-op
    }

    @Override
    protected void rollbackTransaction(Object transaction) {
    }

    @Override
    protected TransactionContextState getTransactionContextState(boolean isJoinTransaction) {
        return new TransactionContextState() {
        };
    }

    // --- Helper methods for registry/diagnostics ---
    public Map<String, Integer> getEntityCounts() {
        Map<String, Integer> map = new HashMap<>();
        for (Map.Entry<String, List<Object>> e : storeData.entrySet()) {
            map.put(e.getKey(), e.getValue().size());
        }
        return map;
    }

    public Map<String, Object> getEntityDefinitions() {
        return Collections.emptyMap();
    }

    // --- Simple entity CRUD for runtime testing ---
    public void saveEntity(String entityName, Object entity) {
        List<Object> list = storeData.computeIfAbsent(entityName, k -> new ArrayList<>());
        UUID id = getEntityId(entity);
        if (id == null) {
            id = UUID.randomUUID();
            setEntityId(entity, id);
        }
        final UUID finalId = id;
        list.removeIf(e -> Objects.equals(getEntityId(e), finalId));
        list.add(entity);
    }

    public List<Object> loadAllEntities(String entityName) {
        return new ArrayList<>(storeData.getOrDefault(entityName, Collections.emptyList()));
    }

    public Object loadEntity(String entityName, UUID id) {
        List<Object> list = storeData.get(entityName);
        if (list == null)
            return null;
        return list.stream().filter(e -> Objects.equals(getEntityId(e), id)).findFirst().orElse(null);
    }

    public void deleteEntity(String entityName, UUID id) {
        List<Object> list = storeData.get(entityName);
        if (list == null)
            return;
        list.removeIf(e -> Objects.equals(getEntityId(e), id));
    }

    private UUID getEntityId(Object entity) {
        if (entity instanceof Map) {
            Object id = ((Map<?, ?>) entity).get("id");
            return (id instanceof UUID) ? (UUID) id : null;
        }
        Object id = EntityValues.getId(entity);
        return (id instanceof UUID) ? (UUID) id : null;
    }

    private void setEntityId(Object entity, UUID id) {
        if (entity instanceof Map) {
            ((Map<String, Object>) entity).put("id", id);
        } else {
            EntityValues.setId(entity, id);
        }
    }
}
