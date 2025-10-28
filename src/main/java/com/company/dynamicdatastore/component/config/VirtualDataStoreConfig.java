package com.company.dynamicdatastore.component.config;

import com.company.dynamicdatastore.component.datastore.VirtualDataStoreManager;
import com.company.dynamicdatastore.example.VirtualDataStoreExample;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualDataStoreConfig {

    private final VirtualDataStoreManager storeManager;
    private final VirtualDataStoreExample example;

    public VirtualDataStoreConfig(VirtualDataStoreManager storeManager,
            VirtualDataStoreExample example) {
        this.storeManager = storeManager;
        this.example = example;
    }

    @PostConstruct
    public void initializeVirtualDataStores() {
        // Tạo store mặc định
        storeManager.createStore("virtualStore1");

        // Chạy example để demo hệ thống
        System.out.println("🚀 Initializing Virtual DataStore System...");
        example.runExample();
        example.demonstrateApiEndpoints();

        System.out.println("✅ Virtual DataStore System initialized successfully!");
    }
}
