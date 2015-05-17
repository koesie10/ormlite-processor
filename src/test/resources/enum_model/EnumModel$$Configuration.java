/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Koen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.koenv.ormlite.processor;

import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

public final class EnumModel$$Configuration {
    public static List<DatabaseFieldConfig> getFieldConfigs() {
        List<DatabaseFieldConfig> list = new ArrayList<DatabaseFieldConfig>();
        DatabaseFieldConfig config = null;
        config = new DatabaseFieldConfig();
        config.setFieldName("name");
        config.setUnknownEnumValue(EnumModel.Test.TEST);
        list.add(config);
        return list;
    }

    public static DatabaseTableConfig<EnumModel> getTableConfig() {
        DatabaseTableConfig<EnumModel> config = new DatabaseTableConfig<EnumModel>();
        config.setDataClass(EnumModel.class);
        config.setTableName("enummodel");
        config.setFieldConfigs(getFieldConfigs());
        return config;
    }
}