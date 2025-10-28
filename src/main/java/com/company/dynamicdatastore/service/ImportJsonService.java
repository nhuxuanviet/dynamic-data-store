package com.company.dynamicdatastore.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("dynamicdatastore_ImportJsonService")
public class ImportJsonService {

    private final VirtualDataStoreService virtualDataStoreService;

    public ImportJsonService(VirtualDataStoreService virtualDataStoreService) {
        this.virtualDataStoreService = virtualDataStoreService;
    }

    public Map<String, Object> importFromUrl(String storeName, String entityName, String url) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        Object payload = response.getBody();
        return importFromJson(storeName, entityName, payload);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> importFromJson(String storeName, String entityName, Object payload) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (payload instanceof List) {
            for (Object item : (List<?>) payload) {
                if (item instanceof Map) {
                    rows.add((Map<String, Object>) item);
                }
            }
        } else if (payload instanceof Map) {
            rows.add((Map<String, Object>) payload);
        } else {
            throw new IllegalArgumentException("Unsupported JSON structure. Expect array or object.");
        }

        if (rows.isEmpty()) {
            return Map.of(
                    "message", "No records to import",
                    "storeName", storeName,
                    "entityName", entityName,
                    "imported", 0);
        }

        Map<String, Object> properties = inferSchema(rows.get(0));
        virtualDataStoreService.registerEntityDefinition(storeName, entityName, properties);
        int imported = virtualDataStoreService.bulkCreateEntities(storeName, entityName, rows);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Imported successfully");
        result.put("storeName", storeName);
        result.put("entityName", entityName);
        result.put("imported", imported);
        result.put("properties", properties);
        return result;
    }

    private Map<String, Object> inferSchema(Map<String, Object> sample) {
        Map<String, Object> props = new HashMap<>();
        for (Map.Entry<String, Object> e : sample.entrySet()) {
            String type = mapJavaTypeToLogical(e.getValue());
            props.put(e.getKey(), Map.of("type", type, "nullable", true));
        }
        return props;
    }

    private String mapJavaTypeToLogical(Object value) {
        if (value == null) return "string";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Integer || value instanceof Long) return "integer";
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) return "bigdecimal";
        if (value instanceof List || value instanceof Map) return "json";
        return "string";
    }
}


