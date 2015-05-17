package com.koenv.ormlite.processor;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.types.BooleanType;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class SimpleModel {
    @DatabaseField
    private String name;

    @DatabaseField(columnName="test", dataType = DataType.BOOLEAN, canBeNull = false, persisterClass = BooleanType.class)
    private boolean simpleBoolean;
}