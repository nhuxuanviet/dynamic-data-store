package com.company.dynamicdatastore.component.entity;

import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * Factory để tạo entities ảo động runtime
 */
@Component("dynamicdatastore_RuntimeEntityFactory")
public class RuntimeEntityFactory {

    private final Metadata metadata;

    public RuntimeEntityFactory(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Tạo entity instance từ VirtualEntityDefinition
     */
    public Object createEntityInstance(VirtualEntityDefinition definition) {
        // Tạo một Map-based entity thay vì sử dụng metadata.create()
        // vì metadata.create() chỉ hoạt động với các entity đã được định nghĩa trước
        Map<String, Object> entity = new HashMap<>();

        // Thiết lập ID mặc định
        entity.put("id", UUID.randomUUID());

        // Thiết lập các thuộc tính mặc định
        for (Map.Entry<String, VirtualEntityDefinition.VirtualPropertyDefinition> entry : definition.getProperties()
                .entrySet()) {
            String propertyName = entry.getKey();
            Class<?> propertyType = entry.getValue().getType();

            Object defaultValue = getDefaultValue(propertyType);
            entity.put(propertyName, defaultValue);
        }

        // Thêm metadata về entity
        entity.put("_entityName", definition.getEntityName());
        entity.put("_storeName", definition.getStoreName());
        entity.put("_definition", definition);

        return entity;
    }

    /**
     * Tạo entity instance với dữ liệu ban đầu
     */
    public Object createEntityInstance(VirtualEntityDefinition definition, Map<String, Object> initialData) {
        Object entity = createEntityInstance(definition);

        // Cập nhật với dữ liệu ban đầu
        if (initialData != null) {
            for (Map.Entry<String, Object> entry : initialData.entrySet()) {
                setEntityValue(entity, entry.getKey(), entry.getValue());
            }
        }

        return entity;
    }

    /**
     * Lấy giá trị mặc định cho kiểu dữ liệu
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == String.class) {
            return "";
        } else if (type == Integer.class || type == int.class) {
            return 0;
        } else if (type == Long.class || type == long.class) {
            return 0L;
        } else if (type == Double.class || type == double.class) {
            return 0.0;
        } else if (type == Float.class || type == float.class) {
            return 0.0f;
        } else if (type == Boolean.class || type == boolean.class) {
            return false;
        } else if (type == UUID.class) {
            return UUID.randomUUID();
        } else if (type == java.math.BigDecimal.class) {
            return java.math.BigDecimal.ZERO;
        } else if (type == java.time.LocalDateTime.class) {
            return java.time.LocalDateTime.now();
        } else if (type == java.time.LocalDate.class) {
            return java.time.LocalDate.now();
        }

        return null;
    }

    /**
     * Thiết lập giá trị cho entity
     */
    public void setEntityValue(Object entity, String propertyName, Object value) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            entityMap.put(propertyName, value);
        } else {
            // Fallback cho các entity không phải Map
            try {
                EntityValues.setValue(entity, propertyName, value);
            } catch (Exception e) {
                System.err.println("Failed to set property " + propertyName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Lấy giá trị từ entity
     */
    public Object getEntityValue(Object entity, String propertyName) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            return entityMap.get(propertyName);
        } else {
            try {
                return EntityValues.getValue(entity, propertyName);
            } catch (Exception e) {
                System.err.println("Failed to get property " + propertyName + ": " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Lấy ID của entity
     */
    public UUID getEntityId(Object entity) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            Object id = entityMap.get("id");
            if (id instanceof UUID) {
                return (UUID) id;
            }
            return null;
        } else {
            try {
                Object id = EntityValues.getId(entity);
                if (id instanceof UUID) {
                    return (UUID) id;
                }
                return null;
            } catch (Exception e) {
                System.err.println("Failed to get entity ID: " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Thiết lập ID cho entity
     */
    public void setEntityId(Object entity, UUID id) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            entityMap.put("id", id);
        } else {
            try {
                EntityValues.setId(entity, id);
            } catch (Exception e) {
                System.err.println("Failed to set entity ID: " + e.getMessage());
            }
        }
    }

    /**
     * Kiểm tra entity có thuộc tính không
     */
    public boolean hasProperty(Object entity, String propertyName) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            return entityMap.containsKey(propertyName);
        }
        return true; // Giả định có thuộc tính cho các entity khác
    }

    /**
     * Lấy tên entity từ instance
     */
    public String getEntityName(Object entity) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            Object entityName = entityMap.get("_entityName");
            return entityName != null ? entityName.toString() : null;
        }
        return entity.getClass().getSimpleName();
    }

    /**
     * Lấy tên store từ instance
     */
    public String getStoreName(Object entity) {
        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entityMap = (Map<String, Object>) entity;
            Object storeName = entityMap.get("_storeName");
            return storeName != null ? storeName.toString() : null;
        }
        return null;
    }
}
