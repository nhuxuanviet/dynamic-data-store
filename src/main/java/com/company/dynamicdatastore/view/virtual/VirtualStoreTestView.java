package com.company.dynamicdatastore.view.virtual;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.company.dynamicdatastore.service.AggregationService;
import com.company.dynamicdatastore.service.VirtualDataStoreService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("virtual-store-test")
@ViewController("dynamicdatastore_VirtualStoreTestView")
@ViewDescriptor("virtual-store-test-view.xml")
public class VirtualStoreTestView extends StandardView {
    @Subscribe(id = "createStoreBtn", subject = "clickListener")
    public void onCreateStoreBtnClick1(final ClickEvent<JmixButton> event) {
        String store = storeNameField.getValue();
        if (store == null || store.isBlank()) {
            Notification.show("Store name is required");
            return;
        }
        service.createStore(store);
        Notification.show("Created store: " + store);
    }

    @Subscribe(id = "registerDefBtn", subject = "clickListener")
    public void onRegisterDefBtnClick1(final ClickEvent<JmixButton> event) {
        String store = storeNameField.getValue();
        String entity = entityNameField.getValue();
        if (store == null || store.isBlank() || entity == null || entity.isBlank()) {
            Notification.show("Store and Entity are required");
            return;
        }
        service.registerEntityDefinition(store, entity, Map.of());
        Notification.show("Registered definition: " + store + "." + entity);
    }

    @Subscribe(id = "createEntityBtn", subject = "clickListener")
    public void onCreateEntityBtnClick1(final ClickEvent<JmixButton> event) {
        String store = storeNameField.getValue();
        String entity = entityNameField.getValue();
        if (store == null || store.isBlank() || entity == null || entity.isBlank()) {
            Notification.show("Store and Entity are required");
            return;
        }
        Map<String, Object> data = parseJson(jsonDataField.getValue());
        service.createEntity(store, entity, data);
        Notification.show("Created entity in " + store + "." + entity);
        loadAll();
    }

    @Subscribe(id = "loadAllBtn", subject = "clickListener")
    public void onLoadAllBtnClick1(final ClickEvent<JmixButton> event) {
        loadAll();
    }

    @Autowired
    private VirtualDataStoreService service;

    @Autowired
    private AggregationService aggregationService;

    @ViewComponent
    private TextField storeNameField;

    @ViewComponent
    private TextField entityNameField;

    @ViewComponent
    private TextArea jsonDataField;

    @ViewComponent
    private TextField entitiesListField;

    @ViewComponent
    private TextField joinKeyField;

    @ViewComponent
    private TextArea selectJsonField;

    @ViewComponent
    private TextArea filtersJsonField;

    @ViewComponent
    private KeyValueCollectionContainer entitiesDc;

    @ViewComponent
    private com.vaadin.flow.component.grid.Grid<io.jmix.core.entity.KeyValueEntity> entitiesGrid;

    @ViewComponent
    private KeyValueCollectionContainer aggregateDc;

    @ViewComponent
    private com.vaadin.flow.component.grid.Grid<io.jmix.core.entity.KeyValueEntity> aggregateGrid;

    @ViewComponent
    private com.vaadin.flow.component.orderedlayout.VerticalLayout configBox;

    private java.util.LinkedHashSet<String> lastAggregateFields = new java.util.LinkedHashSet<>();

    private void loadAll() {
        String store = storeNameField.getValue();
        String entity = entityNameField.getValue();
        if (store == null || store.isBlank() || entity == null || entity.isBlank()) {
            Notification.show("Store and Entity are required");
            return;
        }
        List<Object> list = service.loadAllEntities(store, entity);
        List<Map<String, Object>> maps = service.entitiesToMaps(list);
        entitiesDc.getMutableItems().clear();
        for (Map<String, Object> m : maps) {
            KeyValueEntity e = new KeyValueEntity();
            for (Map.Entry<String, Object> en : m.entrySet()) {
                e.setValue(en.getKey(), en.getValue());
            }
            entitiesDc.getMutableItems().add(e);
        }
        rebuildColumns(entitiesGrid, maps);
    }

    @Subscribe(id = "aggregateBtn", subject = "clickListener")
    public void onAggregateBtnClick(final ClickEvent<JmixButton> event) {
        String store = storeNameField.getValue();
        String entitiesText = entitiesListField.getValue();
        String joinKey = joinKeyField.getValue();
        if (store == null || store.isBlank() || entitiesText == null || entitiesText.isBlank() || joinKey == null || joinKey.isBlank()) {
            Notification.show("Store, Entities and Join key are required");
            return;
        }
        List<String> entities = java.util.Arrays.stream(entitiesText.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        java.util.Map<String, String> select = parseMap(selectJsonField.getValue());
        if (select.isEmpty()) {
            select = new java.util.HashMap<>();
            select.put("joinKey", entities.get(0) + "." + joinKey);
        }
        java.util.Map<String, Object> filters = parseMapObj(filtersJsonField.getValue());

        List<java.util.Map<String, Object>> rows = aggregationService.aggregate(store, entities, joinKey, select, filters);
        aggregateDc.getMutableItems().clear();
        for (java.util.Map<String, Object> m : rows) {
            KeyValueEntity e = new KeyValueEntity();
            for (java.util.Map.Entry<String, Object> en : m.entrySet()) {
                e.setValue(en.getKey(), en.getValue());
            }
            aggregateDc.getMutableItems().add(e);
        }
        if (!rows.isEmpty()) {
            lastAggregateFields.clear();
            lastAggregateFields.addAll(rows.get(0).keySet());
        }
        rebuildColumns(aggregateGrid, rows);
        Notification.show("Aggregated rows: " + rows.size());
    }

    @Subscribe(id = "showConfigBtn", subject = "clickListener")
    public void onShowConfigBtnClick(final ClickEvent<JmixButton> event) {
        // build checklist + filter inputs from current aggregate rows
        configBox.removeAll();
        if (lastAggregateFields.isEmpty()) {
            Notification.show("Aggregate first to configure");
            return;
        }
        for (String f : lastAggregateFields) {
            com.vaadin.flow.component.checkbox.Checkbox cb = new com.vaadin.flow.component.checkbox.Checkbox(f, true);
            cb.setId("cb_" + f);
            com.vaadin.flow.component.textfield.TextField tf = new com.vaadin.flow.component.textfield.TextField();
            tf.setLabel("Filter: " + f);
            tf.setId("filter_" + f);
            configBox.add(cb, tf);
        }
    }

    @Subscribe(id = "applyConfigBtn", subject = "clickListener")
    public void onApplyConfigBtnClick(final ClickEvent<JmixButton> event) {
        // collect selected columns and filters from configBox and re-render aggregate grid
        java.util.List<com.vaadin.flow.component.Component> children = configBox.getChildren().toList();
        java.util.Set<String> selected = new java.util.HashSet<>();
        java.util.Map<String, Object> filters = new java.util.HashMap<>();
        for (com.vaadin.flow.component.Component c : children) {
            if (c instanceof com.vaadin.flow.component.checkbox.Checkbox cb) {
                String field = cb.getLabel();
                if (Boolean.TRUE.equals(cb.getValue())) selected.add(field);
            }
            if (c instanceof com.vaadin.flow.component.textfield.TextField tf) {
                if (tf.getValue() != null && !tf.getValue().isBlank()) {
                    String id = tf.getId().orElse("");
                    if (id.startsWith("filter_")) {
                        String field = id.substring("filter_".length());
                        filters.put(field, tf.getValue());
                    }
                }
            }
        }

        // filter current aggregate rows in-memory and rebuild columns to selected subset
        java.util.List<java.util.Map<String, Object>> current = new java.util.ArrayList<>();
        for (KeyValueEntity kv : aggregateDc.getItems()) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            for (String k : lastAggregateFields) m.put(k, kv.getValue(k));
            current.add(m);
        }
        java.util.List<java.util.Map<String, Object>> filtered = current.stream().filter(m -> {
            for (var e : filters.entrySet()) {
                Object v = m.get(e.getKey());
                if (v == null) return false;
                if (!String.valueOf(v).toLowerCase().contains(String.valueOf(e.getValue()).toLowerCase())) return false;
            }
            return true;
        }).toList();

        aggregateDc.getMutableItems().clear();
        for (java.util.Map<String, Object> m : filtered) {
            KeyValueEntity e2 = new KeyValueEntity();
            if (selected.isEmpty()) {
                for (var en : m.entrySet()) e2.setValue(en.getKey(), en.getValue());
            } else {
                for (String k : selected) e2.setValue(k, m.get(k));
            }
            aggregateDc.getMutableItems().add(e2);
        }
        rebuildColumns(aggregateGrid, filtered.stream().map(m -> selected.isEmpty() ? m : m.entrySet().stream().filter(en -> selected.contains(en.getKey())).collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue))).toList());
    }

    private void rebuildColumns(com.vaadin.flow.component.grid.Grid<io.jmix.core.entity.KeyValueEntity> grid, java.util.List<java.util.Map<String, Object>> rows) {
        if (grid == null) return;
        grid.removeAllColumns();
        if (rows == null || rows.isEmpty()) return;
        java.util.Map<String, Object> sample = rows.get(0);
        for (String key : sample.keySet()) {
            grid.addColumn(item -> {
                Object v = item.getValue(key);
                return v == null ? "" : String.valueOf(v);
            }).setHeader(key).setAutoWidth(true).setSortable(true);
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, String> parseMap(String json) {
        try {
            if (json == null || json.isBlank()) return java.util.Collections.emptyMap();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            Notification.show("Invalid select JSON, ignoring");
            return java.util.Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> parseMapObj(String json) {
        try {
            if (json == null || json.isBlank()) return java.util.Collections.emptyMap();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, java.util.Map.class);
        } catch (Exception e) {
            Notification.show("Invalid filters JSON, ignoring");
            return java.util.Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            if (json == null || json.isBlank()) {
                return java.util.Collections.emptyMap();
            }
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            Notification.show("Invalid JSON, using empty object");
            return java.util.Collections.emptyMap();
        }
    }
}
