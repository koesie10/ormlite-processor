package com.koenv.ormlite.processor;

import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable
public class ForeignCollectionModel {
    @ForeignCollectionField(eager = false)
    private List<ForeignCollectionModelForeign> foreigns;
}