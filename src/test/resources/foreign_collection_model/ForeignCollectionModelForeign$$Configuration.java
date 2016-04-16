package com.koenv.ormlite.processor;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.BooleanType;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

public final class ForeignCollectionModelForeign$$Configuration {
    public static List<DatabaseFieldConfig> getFieldConfigs() {
        List<DatabaseFieldConfig> list = new ArrayList<DatabaseFieldConfig>();
        DatabaseFieldConfig config = null;
        config = new DatabaseFieldConfig();
        config.setFieldName("name");
        list.add(config);
        return list;
    }

    public static DatabaseTableConfig<ForeignCollectionModelForeign> getTableConfig() {
        DatabaseTableConfig<ForeignCollectionModelForeign> config = new DatabaseTableConfig<ForeignCollectionModelForeign>();
        config.setDataClass(ForeignCollectionModelForeign.class);
        config.setTableName("foreigncollectionmodelforeign");
        config.setFieldConfigs(getFieldConfigs());
        return config;
    }
}