# Virtual DataStore System

Hệ thống Virtual DataStore cho phép tạo và quản lý datastore ảo runtime với entities ảo động và API loadall/loadone runtime.

## Tính năng chính

- ✅ **Datastore ảo runtime**: Tạo và quản lý nhiều datastore ảo động
- ✅ **Entities ảo động**: Định nghĩa entities với thuộc tính động tại runtime
- ✅ **API loadall/loadone**: API RESTful để thao tác với dữ liệu
- ✅ **CRUD Operations**: Create, Read, Update, Delete entities
- ✅ **Multi-store support**: Hỗ trợ nhiều datastore đồng thời
- ✅ **Type safety**: Hỗ trợ các kiểu dữ liệu cơ bản

## Kiến trúc hệ thống

```
VirtualDataStoreService (API Layer)
    ↓
VirtualDataStoreManager (Management Layer)
    ↓
VirtualDataStore (Storage Layer)
    ↓
RuntimeEntityFactory (Entity Creation)
    ↓
VirtualEntityDefinition (Entity Schema)
```

## Cách sử dụng

### 1. Tạo Store

```java
// Tạo store mới
String storeName = virtualDataStoreService.createStore("my-store");
```

### 2. Đăng ký Entity Definition

```java
// Định nghĩa thuộc tính cho entity
Map<String, Object> properties = new HashMap<>();
properties.put("name", Map.of("type", "string", "nullable", false));
properties.put("price", Map.of("type", "bigdecimal", "nullable", false));
properties.put("description", Map.of("type", "string", "nullable", true));

// Đăng ký entity definition
virtualDataStoreService.registerEntityDefinition("my-store", "Product", properties);
```

### 3. Tạo và Lưu Entity

```java
// Tạo dữ liệu entity
Map<String, Object> productData = new HashMap<>();
productData.put("name", "iPhone 15 Pro");
productData.put("price", "999.99");
productData.put("description", "Latest iPhone");

// Tạo và lưu entity
Object product = virtualDataStoreService.createEntity("my-store", "Product", productData);
```

### 4. Load Dữ liệu

```java
// Load tất cả entities
List<Object> allProducts = virtualDataStoreService.loadAllEntities("my-store", "Product");

// Load entity theo ID
Object product = virtualDataStoreService.loadEntity("my-store", "Product", productId);
```

### 5. Cập nhật Entity

```java
// Cập nhật dữ liệu
Map<String, Object> updateData = new HashMap<>();
updateData.put("price", "899.99");

// Cập nhật entity
Object updatedProduct = virtualDataStoreService.updateEntity("my-store", "Product", productId, updateData);
```

### 6. Xóa Entity

```java
// Xóa entity
virtualDataStoreService.deleteEntity("my-store", "Product", productId);
```

## REST API Endpoints

### Store Management
- `POST /api/virtual-datastore/stores` - Tạo store mới
- `GET /api/virtual-datastore/stores` - Lấy danh sách stores
- `DELETE /api/virtual-datastore/stores/{storeName}` - Xóa store

### Entity Definition Management
- `POST /api/virtual-datastore/stores/{storeName}/entities` - Đăng ký entity definition
- `GET /api/virtual-datastore/stores/{storeName}/entities` - Lấy entity definitions
- `GET /api/virtual-datastore/stores/{storeName}/entities/{entityName}/definition` - Lấy entity definition cụ thể
- `DELETE /api/virtual-datastore/stores/{storeName}/entities/{entityName}` - Xóa entity definition

### Entity Operations
- `POST /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data` - Tạo entity
- `GET /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data` - Load tất cả entities
- `GET /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data/{id}` - Load entity theo ID
- `PUT /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data/{id}` - Cập nhật entity
- `DELETE /api/virtual-datastore/stores/{storeName}/entities/{entityName}/data/{id}` - Xóa entity

### Utility
- `GET /api/virtual-datastore/stores/{storeName}/statistics` - Thống kê store
- `GET /api/virtual-datastore/health` - Health check

## Ví dụ API Request/Response

### Tạo Store
```bash
POST /api/virtual-datastore/stores
Content-Type: application/json

{
  "name": "ecommerce-store"
}
```

Response:
```json
{
  "message": "Store created successfully",
  "storeName": "ecommerce-store"
}
```

### Đăng ký Entity Definition
```bash
POST /api/virtual-datastore/stores/ecommerce-store/entities
Content-Type: application/json

{
  "entityName": "Product",
  "properties": {
    "name": {"type": "string", "nullable": false},
    "price": {"type": "bigdecimal", "nullable": false},
    "description": {"type": "string", "nullable": true}
  }
}
```

### Tạo Entity
```bash
POST /api/virtual-datastore/stores/ecommerce-store/entities/Product/data
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "price": "999.99",
  "description": "Latest iPhone"
}
```

### Load All Entities
```bash
GET /api/virtual-datastore/stores/ecommerce-store/entities/Product/data
```

Response:
```json
{
  "entities": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "iPhone 15 Pro",
      "price": "999.99",
      "description": "Latest iPhone"
    }
  ],
  "count": 1
}
```

## Kiểu dữ liệu hỗ trợ

- `string` - String
- `integer`/`int` - Integer
- `long` - Long
- `double` - Double
- `float` - Float
- `boolean`/`bool` - Boolean
- `uuid` - UUID
- `bigdecimal` - BigDecimal
- `localdatetime` - LocalDateTime
- `localdate` - LocalDate

## Cấu trúc thư mục

```
src/main/java/com/company/dynamicdatastore/
├── component/
│   ├── config/
│   │   └── VirtualDataStoreConfig.java
│   ├── datastore/
│   │   ├── VirtualDataStore.java
│   │   └── VirtualDataStoreManager.java
│   ├── entity/
│   │   ├── VirtualEntityDefinition.java
│   │   └── RuntimeEntityFactory.java
│   └── registy/
│       └── DataStoreRegistry.java
├── controller/
│   └── VirtualDataStoreController.java
├── service/
│   └── VirtualDataStoreService.java
└── example/
    └── VirtualDataStoreExample.java
```

## Chạy Example

Hệ thống sẽ tự động chạy example khi khởi động ứng dụng. Example sẽ:

1. Tạo 2 stores: `ecommerce-store` và `inventory-store`
2. Đăng ký entity definitions cho Product, Order, và InventoryItem
3. Tạo và lưu sample data
4. Load và hiển thị dữ liệu
5. Cập nhật entity
6. Hiển thị thống kê

## Lưu ý

- Tất cả entities được lưu trong memory (không persist)
- Hệ thống hỗ trợ concurrent access
- Entity IDs được tự động generate dưới dạng UUID
- API trả về JSON format
- Hỗ trợ error handling và validation
