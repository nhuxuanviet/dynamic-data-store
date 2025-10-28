package com.company.dynamicdatastore.example;

import com.company.dynamicdatastore.component.datastore.VirtualDataStoreManager;
import com.company.dynamicdatastore.service.VirtualDataStoreService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Example usage của Virtual DataStore System
 */
@Component("dynamicdatastore_VirtualDataStoreExample")
public class VirtualDataStoreExample {

        private final VirtualDataStoreService virtualDataStoreService;
        private final VirtualDataStoreManager storeManager;

        public VirtualDataStoreExample(VirtualDataStoreService virtualDataStoreService,
                        VirtualDataStoreManager storeManager) {
                this.virtualDataStoreService = virtualDataStoreService;
                this.storeManager = storeManager;
        }

        /**
         * Demo cách sử dụng Virtual DataStore System
         */
        public void runExample() {
                System.out.println("🚀 Starting Virtual DataStore Example...");

                try {
                        // 1. Tạo store mới
                        System.out.println("\n📦 Step 1: Creating stores...");
                        String storeName1 = virtualDataStoreService.createStore("ecommerce-store");
                        String storeName2 = virtualDataStoreService.createStore("inventory-store");

                        // 2. Đăng ký entity definitions
                        System.out.println("\n🏗️ Step 2: Registering entity definitions...");

                        // Product entity cho ecommerce-store
                        Map<String, Object> productProperties = new HashMap<>();
                        productProperties.put("name", Map.of("type", "string", "nullable", false));
                        productProperties.put("price", Map.of("type", "bigdecimal", "nullable", false));
                        productProperties.put("description", Map.of("type", "string", "nullable", true));
                        productProperties.put("category", Map.of("type", "string", "nullable", true));
                        productProperties.put("inStock", Map.of("type", "boolean", "nullable", false));

                        virtualDataStoreService.registerEntityDefinition(storeName1, "Product", productProperties);

                        // Order entity cho ecommerce-store
                        Map<String, Object> orderProperties = new HashMap<>();
                        orderProperties.put("orderNumber", Map.of("type", "string", "nullable", false));
                        orderProperties.put("customerName", Map.of("type", "string", "nullable", false));
                        orderProperties.put("totalAmount", Map.of("type", "bigdecimal", "nullable", false));
                        orderProperties.put("orderDate", Map.of("type", "localdatetime", "nullable", false));
                        orderProperties.put("status", Map.of("type", "string", "nullable", false));

                        virtualDataStoreService.registerEntityDefinition(storeName1, "Order", orderProperties);

                        // InventoryItem entity cho inventory-store
                        Map<String, Object> inventoryProperties = new HashMap<>();
                        inventoryProperties.put("itemCode", Map.of("type", "string", "nullable", false));
                        inventoryProperties.put("itemName", Map.of("type", "string", "nullable", false));
                        inventoryProperties.put("quantity", Map.of("type", "integer", "nullable", false));
                        inventoryProperties.put("unitPrice", Map.of("type", "bigdecimal", "nullable", false));
                        inventoryProperties.put("supplier", Map.of("type", "string", "nullable", true));

                        virtualDataStoreService.registerEntityDefinition(storeName2, "InventoryItem",
                                        inventoryProperties);

                        // 3. Tạo và lưu entities
                        System.out.println("\n💾 Step 3: Creating and saving entities...");

                        // Tạo products
                        Map<String, Object> product1Data = new HashMap<>();
                        product1Data.put("name", "iPhone 15 Pro");
                        product1Data.put("price", "999.99");
                        product1Data.put("description", "Latest iPhone with advanced features");
                        product1Data.put("category", "Electronics");
                        product1Data.put("inStock", true);

                        Object product1 = virtualDataStoreService.createEntity(storeName1, "Product", product1Data);

                        Map<String, Object> product2Data = new HashMap<>();
                        product2Data.put("name", "MacBook Pro M3");
                        product2Data.put("price", "1999.99");
                        product2Data.put("description", "Powerful laptop for professionals");
                        product2Data.put("category", "Electronics");
                        product2Data.put("inStock", true);

                        virtualDataStoreService.createEntity(storeName1, "Product", product2Data);

                        // Tạo orders
                        Map<String, Object> order1Data = new HashMap<>();
                        order1Data.put("orderNumber", "ORD-001");
                        order1Data.put("customerName", "John Doe");
                        order1Data.put("totalAmount", "999.99");
                        order1Data.put("orderDate", "2024-01-15T10:30:00");
                        order1Data.put("status", "Completed");

                        virtualDataStoreService.createEntity(storeName1, "Order", order1Data);

                        // Tạo inventory items
                        Map<String, Object> inventory1Data = new HashMap<>();
                        inventory1Data.put("itemCode", "INV-001");
                        inventory1Data.put("itemName", "iPhone 15 Pro");
                        inventory1Data.put("quantity", 50);
                        inventory1Data.put("unitPrice", "800.00");
                        inventory1Data.put("supplier", "Apple Inc.");

                        virtualDataStoreService.createEntity(storeName2, "InventoryItem", inventory1Data);

                        // 4. Load và hiển thị dữ liệu
                        System.out.println("\n📋 Step 4: Loading and displaying data...");

                        // Load tất cả products
                        List<Object> allProducts = virtualDataStoreService.loadAllEntities(storeName1, "Product");
                        System.out.println("📱 Products in " + storeName1 + ":");
                        for (Object product : allProducts) {
                                Map<String, Object> productMap = virtualDataStoreService.entityToMap(product);
                                System.out.println("  - " + productMap.get("name") + " - $" + productMap.get("price"));
                        }

                        // Load tất cả orders
                        List<Object> allOrders = virtualDataStoreService.loadAllEntities(storeName1, "Order");
                        System.out.println("\n📦 Orders in " + storeName1 + ":");
                        for (Object order : allOrders) {
                                Map<String, Object> orderMap = virtualDataStoreService.entityToMap(order);
                                System.out.println("  - " + orderMap.get("orderNumber") + " - "
                                                + orderMap.get("customerName") + " - $"
                                                + orderMap.get("totalAmount"));
                        }

                        // Load tất cả inventory items
                        List<Object> allInventory = virtualDataStoreService.loadAllEntities(storeName2,
                                        "InventoryItem");
                        System.out.println("\n📊 Inventory items in " + storeName2 + ":");
                        for (Object item : allInventory) {
                                Map<String, Object> itemMap = virtualDataStoreService.entityToMap(item);
                                System.out.println("  - " + itemMap.get("itemCode") + " - " + itemMap.get("itemName")
                                                + " - Qty: "
                                                + itemMap.get("quantity"));
                        }

                        // 5. Cập nhật entity
                        System.out.println("\n✏️ Step 5: Updating entities...");

                        // Lấy ID của product đầu tiên
                        UUID product1Id = UUID.fromString(
                                        ((Map<String, Object>) virtualDataStoreService.entityToMap(product1).get("id"))
                                                        .toString());

                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("price", "899.99"); // Giảm giá
                        updateData.put("description", "iPhone 15 Pro - Special Sale Price!");

                        Object updatedProduct = virtualDataStoreService.updateEntity(storeName1, "Product",
                                        product1Id.toString(),
                                        updateData);
                        Map<String, Object> updatedProductMap = virtualDataStoreService.entityToMap(updatedProduct);
                        System.out.println("✅ Updated product: " + updatedProductMap.get("name") + " - New price: $"
                                        + updatedProductMap.get("price"));

                        // 6. Thống kê
                        System.out.println("\n📊 Step 6: Store statistics...");
                        Map<String, Integer> store1Stats = virtualDataStoreService.getStoreStatistics(storeName1);
                        Map<String, Integer> store2Stats = virtualDataStoreService.getStoreStatistics(storeName2);

                        System.out.println("📈 " + storeName1 + " statistics:");
                        store1Stats.forEach(
                                        (entityType, count) -> System.out
                                                        .println("  - " + entityType + ": " + count + " entities"));

                        System.out.println("📈 " + storeName2 + " statistics:");
                        store2Stats.forEach(
                                        (entityType, count) -> System.out
                                                        .println("  - " + entityType + ": " + count + " entities"));

                        // 7. Load một entity cụ thể
                        System.out.println("\n🔍 Step 7: Loading specific entity...");
                        Object loadedProduct = virtualDataStoreService.loadEntity(storeName1, "Product",
                                        product1Id.toString());
                        if (loadedProduct != null) {
                                Map<String, Object> loadedProductMap = virtualDataStoreService
                                                .entityToMap(loadedProduct);
                                System.out.println("🔍 Loaded product details:");
                                loadedProductMap.forEach(
                                                (key, value) -> System.out.println("  - " + key + ": " + value));
                        }

                        System.out.println("\n✅ Virtual DataStore Example completed successfully!");

                } catch (Exception e) {
                        System.err.println("❌ Error in Virtual DataStore Example: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        /**
         * Demo API endpoints (giả lập HTTP requests)
         */
        public void demonstrateApiEndpoints() {
                System.out.println("\n🌐 API Endpoints Demo:");
                System.out.println("POST /api/virtual-datastore/stores - Create store");
                System.out.println("GET /api/virtual-datastore/stores - List all stores");
                System.out.println(
                                "POST /api/virtual-datastore/stores/{storeName}/entities - Register entity definition");
                System.out.println("GET /api/virtual-datastore/stores/{storeName}/entities - List entity definitions");
                System.out.println(
                                "POST /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data - Create entity");
                System.out.println(
                                "GET /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data - Load all entities");
                System.out.println(
                                "GET /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data/{id} - Load specific entity");
                System.out.println(
                                "PUT /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data/{id} - Update entity");
                System.out.println(
                                "DELETE /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data/{id} - Delete entity");
                System.out.println("GET /api/virtual-datastore/stores/{storeName}/statistics - Get store statistics");
                System.out.println("GET /api/virtual-datastore/health - Health check");
        }
}
