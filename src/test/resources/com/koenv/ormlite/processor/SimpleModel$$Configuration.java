package com.koenv.ormlite.processor;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.BooleanType;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

public final class SimpleModel$$Configuration {
    public static List<DatabaseFieldConfig> getFieldConfigs() {
        List<DatabaseFieldConfig> list = new ArrayList<DatabaseFieldConfig>();
        DatabaseFieldConfig config1 = new DatabaseFieldConfig();
        config1.setFieldName("name");
        list.add(config1);
        DatabaseFieldConfig config2 = new DatabaseFieldConfig();
        config2.setFieldName("simpleBoolean");
        config2.setColumnName("test");
        config2.setDataType(DataType.BOOLEAN);
        config2.setCanBeNull(false);
        config2.setPersisterClass(BooleanType.class);
        list.add(config2);
        return list;
    }

    public static DatabaseTableConfig<SimpleModel> getTableConfig() {
        DatabaseTableConfig<SimpleModel> config = new DatabaseTableConfig<SimpleModel>();
        config.setDataClass(SimpleModel.class);
        config.setTableName("simplemodel");
        config.setFieldConfigs(getFieldConfigs());
        return config;
    }
}