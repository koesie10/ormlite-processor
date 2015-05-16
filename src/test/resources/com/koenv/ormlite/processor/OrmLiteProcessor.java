package com.koenv.ormlite.processor;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.DatabaseTableConfig;
import java.util.ArrayList;
import java.util.Collection;

public final class OrmLiteProcessor {
    public static void init() {
        Collection<DatabaseTableConfig<?>> configs = new ArrayList<DatabaseTableConfig<?>>();
        configs.add(SimpleModel$$Configuration.getTableConfig());
        DaoManager.addCachedDatabaseConfigs(configs);
    }
}