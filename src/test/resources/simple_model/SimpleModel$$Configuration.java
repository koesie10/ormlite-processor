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
        DatabaseFieldConfig config = null;
        config = new DatabaseFieldConfig();
        config.setFieldName("name");
        list.add(config);
        config = new DatabaseFieldConfig();
        config.setFieldName("simpleBoolean");
        config.setColumnName("test");
        config.setDataType(DataType.BOOLEAN);
        config.setCanBeNull(false);
        config.setPersisterClass(BooleanType.class);
        list.add(config);
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