package com.koenv.ormlite.processor;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.table.DatabaseTableConfig;
import java.util.ArrayList;
import java.util.List;

public final class ForeignCollectionModel$$Configuration {
    public static List<DatabaseFieldConfig> getFieldConfigs() {
        List<DatabaseFieldConfig> list = new ArrayList<DatabaseFieldConfig>();
        DatabaseFieldConfig config = null;
        config = new DatabaseFieldConfig();
        config.setFieldName("foreigns");
        config.setForeignCollection(true);
        list.add(config);
        return list;
    }

    public static DatabaseTableConfig<ForeignCollectionModel> getTableConfig() {
        DatabaseTableConfig<ForeignCollectionModel> config = new DatabaseTableConfig<ForeignCollectionModel>();
        config.setDataClass(ForeignCollectionModel.class);
        config.setTableName("foreigncollectionmodel");
        config.setFieldConfigs(getFieldConfigs());
        return config;
    }
}