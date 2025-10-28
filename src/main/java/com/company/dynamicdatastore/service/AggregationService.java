package com.company.dynamicdatastore.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("dynamicdatastore_AggregationService")
public class AggregationService {

    private final VirtualDataStoreService dataService;

    public AggregationService(VirtualDataStoreService dataService) {
        this.dataService = dataService;
    }

    /**
     * Gộp nhiều entity trong cùng một store theo khóa chung (joinKey).
     * - entities: danh sách entity names cần gộp, ví dụ ["Citizen","Education","Health","Marriage"]
     * - joinKey: tên trường chung có mặt ở tất cả entity, ví dụ "cccd"
     * - select: map tên alias -> tham chiếu field dạng "Entity.field"
     * - filters: bộ lọc equals theo field dạng "Entity.field" -> value
     */
    public List<Map<String, Object>> aggregate(String storeName,
                                               List<String> entities,
                                               String joinKey,
                                               Map<String, String> select,
                                               Map<String, Object> filters) {
        if (entities == null || entities.isEmpty()) return List.of();

        // Load tất cả dữ liệu theo entity
        Map<String, List<Map<String, Object>>> entityToRows = new HashMap<>();
        for (String entity : entities) {
            List<Object> list = dataService.loadAllEntities(storeName, entity);
            List<Map<String, Object>> maps = dataService.entitiesToMaps(list);
            entityToRows.put(entity, maps);
        }

        // Dựa trên entity đầu tiên làm tập gốc, join tuần tự các entity còn lại theo joinKey
        String base = entities.get(0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> baseRow : entityToRows.getOrDefault(base, List.of())) {
            Object key = baseRow.get(joinKey);
            if (key == null) continue;

            Map<String, Object> merged = new HashMap<>();
            merged.put(base + ".*", baseRow); // giữ bản gốc để tham chiếu nếu cần
            // copy phẳng với prefix
            flattenInto(merged, base, baseRow);

            boolean missing = false;
            for (int i = 1; i < entities.size(); i++) {
                String e = entities.get(i);
                Map<String, Object> matched = findByJoinKey(entityToRows.getOrDefault(e, List.of()), joinKey, key);
                if (matched == null) { missing = true; break; }
                merged.put(e + ".*", matched);
                flattenInto(merged, e, matched);
            }
            if (missing) continue;

            if (!matchesFilters(merged, filters)) continue;

            // Ánh xạ select alias -> value từ merged
            Map<String, Object> projection = project(merged, select);
            result.add(projection);
        }
        return result;
    }

    private Map<String, Object> project(Map<String, Object> merged, Map<String, String> select) {
        if (select == null || select.isEmpty()) return merged;
        Map<String, Object> out = new HashMap<>();
        for (Map.Entry<String, String> e : select.entrySet()) {
            out.put(e.getKey(), merged.get(e.getValue()));
        }
        return out;
    }

    private void flattenInto(Map<String, Object> target, String prefix, Map<String, Object> source) {
        for (Map.Entry<String, Object> en : source.entrySet()) {
            target.put(prefix + "." + en.getKey(), en.getValue());
        }
    }

    private Map<String, Object> findByJoinKey(List<Map<String, Object>> list, String joinKey, Object key) {
        for (Map<String, Object> m : list) {
            if (Objects.equals(m.get(joinKey), key)) return m;
        }
        return null;
    }

    private boolean matchesFilters(Map<String, Object> row, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return true;
        for (Map.Entry<String, Object> f : filters.entrySet()) {
            Object v = row.get(f.getKey());
            if (f.getValue() == null) { if (v != null) return false; }
            else {
                if (v == null) return false;
                if (!String.valueOf(v).equalsIgnoreCase(String.valueOf(f.getValue()))) return false;
            }
        }
        return true;
    }
}


