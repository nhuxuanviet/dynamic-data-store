package com.company.dynamicdatastore.component.entity;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Định nghĩa entity ảo runtime với các thuộc tính động
 */
public class VirtualEntityDefinition {

    private String entityName;
    private String storeName;
    private Map<String, VirtualPropertyDefinition> properties;
    private List<String> primaryKeyProperties;

    public VirtualEntityDefinition(String entityName, String storeName) {
        this.entityName = entityName;
        this.storeName = storeName;
        this.properties = new HashMap<>();
        this.primaryKeyProperties = new ArrayList<>();
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Map<String, VirtualPropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, VirtualPropertyDefinition> properties) {
        this.properties = properties;
    }

    public List<String> getPrimaryKeyProperties() {
        return primaryKeyProperties;
    }

    public void setPrimaryKeyProperties(List<String> primaryKeyProperties) {
        this.primaryKeyProperties = primaryKeyProperties;
    }

    public void addProperty(String name, Class<?> type, boolean nullable) {
        properties.put(name, new VirtualPropertyDefinition(name, type, nullable));
    }

    public void addProperty(String name, Class<?> type) {
        addProperty(name, type, true);
    }

    public void addPrimaryKeyProperty(String propertyName) {
        if (!primaryKeyProperties.contains(propertyName)) {
            primaryKeyProperties.add(propertyName);
        }
    }

    public VirtualPropertyDefinition getProperty(String name) {
        return properties.get(name);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public String toString() {
        return String.format("VirtualEntityDefinition{entityName='%s', storeName='%s', properties=%d}",
                entityName, storeName, properties.size());
    }

    /**
     * Định nghĩa thuộc tính của entity ảo
     */
    public static class VirtualPropertyDefinition {
        private String name;
        private Class<?> type;
        private boolean nullable;

        public VirtualPropertyDefinition(String name, Class<?> type, boolean nullable) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        @Override
        public String toString() {
            return String.format("VirtualPropertyDefinition{name='%s', type=%s, nullable=%s}",
                    name, type.getSimpleName(), nullable);
        }
    }
}
