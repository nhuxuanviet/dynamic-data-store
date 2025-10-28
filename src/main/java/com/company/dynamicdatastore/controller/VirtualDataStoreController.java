package com.company.dynamicdatastore.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.dynamicdatastore.service.AggregationService;
import com.company.dynamicdatastore.service.ImportJsonService;
import com.company.dynamicdatastore.service.VirtualDataStoreService;

/**
 * REST Controller để expose API cho Virtual DataStore
 */
@RestController
@RequestMapping("/api/virtual-datastore")
public class VirtualDataStoreController {

    private final VirtualDataStoreService virtualDataStoreService;
    private final ImportJsonService importJsonService;
    private final AggregationService aggregationService;

    public VirtualDataStoreController(VirtualDataStoreService virtualDataStoreService,
            ImportJsonService importJsonService,
            AggregationService aggregationService) {
        this.virtualDataStoreService = virtualDataStoreService;
        this.importJsonService = importJsonService;
        this.aggregationService = aggregationService;
    }

    // ========== STORE MANAGEMENT ==========

    /**
     * Tạo datastore mới
     */
    @PostMapping("/stores")
    public ResponseEntity<Map<String, Object>> createStore(@RequestBody Map<String, String> request) {
        String storeName = request.get("name");
        if (storeName == null || storeName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Store name is required"));
        }

        try {
            String createdStoreName = virtualDataStoreService.createStore(storeName);
            return ResponseEntity.ok(Map.of(
                    "message", "Store created successfully",
                    "storeName", createdStoreName));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả stores
     */
    @GetMapping("/stores")
    public ResponseEntity<Map<String, Object>> getAllStores() {
        Map<String, Object> stores = virtualDataStoreService.getAllStores();
        return ResponseEntity.ok(Map.of("stores", stores));
    }

    /**
     * Xóa store
     */
    @DeleteMapping("/stores/{storeName}")
    public ResponseEntity<Map<String, Object>> deleteStore(@PathVariable String storeName) {
        try {
            virtualDataStoreService.deleteStore(storeName);
            return ResponseEntity.ok(Map.of(
                    "message", "Store deleted successfully",
                    "storeName", storeName));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENTITY DEFINITION MANAGEMENT ==========

    /**
     * Đăng ký entity definition
     */
    @PostMapping("/stores/{storeName}/entities")
    public ResponseEntity<Map<String, Object>> registerEntityDefinition(
            @PathVariable String storeName,
            @RequestBody Map<String, Object> request) {

        String entityName = (String) request.get("entityName");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) request.get("properties");

        if (entityName == null || entityName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Entity name is required"));
        }

        if (properties == null || properties.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Properties are required"));
        }

        try {
            virtualDataStoreService.registerEntityDefinition(storeName, entityName, properties);
            return ResponseEntity.ok(Map.of(
                    "message", "Entity definition registered successfully",
                    "storeName", storeName,
                    "entityName", entityName));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy entity definition
     */
    @GetMapping("/stores/{storeName}/entities/{entityName}/definition")
    public ResponseEntity<Map<String, Object>> getEntityDefinition(
            @PathVariable String storeName,
            @PathVariable String entityName) {

        try {
            var definition = virtualDataStoreService.getEntityDefinition(storeName, entityName);
            if (definition == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> result = Map.of(
                    "entityName", definition.getEntityName(),
                    "storeName", definition.getStoreName(),
                    "properties", definition.getProperties(),
                    "primaryKeyProperties", definition.getPrimaryKeyProperties());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tất cả entity definitions của store
     */
    @GetMapping("/stores/{storeName}/entities")
    public ResponseEntity<Map<String, Object>> getEntityDefinitions(@PathVariable String storeName) {
        try {
            List<Map<String, Object>> definitions = virtualDataStoreService.getEntityDefinitions(storeName)
                    .stream()
                    .map(def -> Map.of(
                            "entityName", def.getEntityName(),
                            "storeName", def.getStoreName(),
                            "properties", def.getProperties(),
                            "primaryKeyProperties", def.getPrimaryKeyProperties()))
                    .toList();

            return ResponseEntity.ok(Map.of("entityDefinitions", definitions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa entity definition
     */
    @DeleteMapping("/stores/{storeName}/entities/{entityName}")
    public ResponseEntity<Map<String, Object>> deleteEntityDefinition(
            @PathVariable String storeName,
            @PathVariable String entityName) {

        try {
            virtualDataStoreService.deleteEntityDefinition(storeName, entityName);
            return ResponseEntity.ok(Map.of(
                    "message", "Entity definition deleted successfully",
                    "storeName", storeName,
                    "entityName", entityName));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENTITY OPERATIONS ==========

    /**
     * Tạo entity mới
     */
    @PostMapping("/stores/{storeName}/entities/{entityName}/data")
    public ResponseEntity<Map<String, Object>> createEntity(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @RequestBody Map<String, Object> data) {

        try {
            Object entity = virtualDataStoreService.createEntity(storeName, entityName, data);
            Map<String, Object> entityMap = virtualDataStoreService.entityToMap(entity);

            return ResponseEntity.ok(Map.of(
                    "message", "Entity created successfully",
                    "entity", entityMap));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Load tất cả entities của một loại
     */
    @GetMapping("/stores/{storeName}/entities/{entityName}/data")
    public ResponseEntity<Map<String, Object>> loadAllEntities(
            @PathVariable String storeName,
            @PathVariable String entityName) {

        try {
            List<Object> entities = virtualDataStoreService.loadAllEntities(storeName, entityName);
            List<Map<String, Object>> entityMaps = virtualDataStoreService.entitiesToMaps(entities);

            return ResponseEntity.ok(Map.of(
                    "entities", entityMaps,
                    "count", entityMaps.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Load một entity theo ID
     */
    @GetMapping("/stores/{storeName}/entities/{entityName}/data/{id}")
    public ResponseEntity<Map<String, Object>> loadEntity(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @PathVariable String id) {

        try {
            Object entity = virtualDataStoreService.loadEntity(storeName, entityName, id);
            if (entity == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> entityMap = virtualDataStoreService.entityToMap(entity);
            return ResponseEntity.ok(Map.of("entity", entityMap));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cập nhật entity
     */
    @PutMapping("/stores/{storeName}/entities/{entityName}/data/{id}")
    public ResponseEntity<Map<String, Object>> updateEntity(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {

        try {
            Object entity = virtualDataStoreService.updateEntity(storeName, entityName, id, data);
            Map<String, Object> entityMap = virtualDataStoreService.entityToMap(entity);

            return ResponseEntity.ok(Map.of(
                    "message", "Entity updated successfully",
                    "entity", entityMap));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa entity
     */
    @DeleteMapping("/stores/{storeName}/entities/{entityName}/data/{id}")
    public ResponseEntity<Map<String, Object>> deleteEntity(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @PathVariable String id) {

        try {
            virtualDataStoreService.deleteEntity(storeName, entityName, id);
            return ResponseEntity.ok(Map.of(
                    "message", "Entity deleted successfully",
                    "storeName", storeName,
                    "entityName", entityName,
                    "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== IMPORT & QUERY ==========

    @PostMapping("/stores/{storeName}/entities/{entityName}/import/url")
    public ResponseEntity<Map<String, Object>> importFromUrl(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @RequestBody Map<String, Object> body) {
        try {
            String url = String.valueOf(body.get("url"));
            Map<String, Object> result = importJsonService.importFromUrl(storeName, entityName, url);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stores/{storeName}/entities/{entityName}/import/json")
    public ResponseEntity<Map<String, Object>> importFromJson(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @RequestBody Object payload) {
        try {
            Map<String, Object> result = importJsonService.importFromJson(storeName, entityName, payload);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stores/{storeName}/entities/{entityName}/query")
    public ResponseEntity<Map<String, Object>> query(
            @PathVariable String storeName,
            @PathVariable String entityName,
            @RequestBody(required = false) Map<String, Object> filters) {
        try {
            List<Map<String, Object>> items = virtualDataStoreService.query(storeName, entityName, filters);
            return ResponseEntity.ok(Map.of("entities", items, "count", items.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stores/{storeName}/aggregate")
    public ResponseEntity<Map<String, Object>> aggregate(
            @PathVariable String storeName,
            @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> entities = (List<String>) body.get("entities");
            String joinKey = String.valueOf(body.get("joinKey"));
            @SuppressWarnings("unchecked")
            Map<String, String> select = (Map<String, String>) body.get("select");
            @SuppressWarnings("unchecked")
            Map<String, Object> filters = (Map<String, Object>) body.get("filters");

            List<Map<String, Object>> rows = aggregationService.aggregate(storeName, entities, joinKey, select, filters);
            return ResponseEntity.ok(Map.of("rows", rows, "count", rows.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== UTILITY ENDPOINTS ==========

    /**
     * Lấy thống kê store
     */
    @GetMapping("/stores/{storeName}/statistics")
    public ResponseEntity<Map<String, Object>> getStoreStatistics(@PathVariable String storeName) {
        try {
            Map<String, Integer> statistics = virtualDataStoreService.getStoreStatistics(storeName);
            return ResponseEntity.ok(Map.of("statistics", statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Virtual DataStore API",
                "timestamp", System.currentTimeMillis()));
    }
}
