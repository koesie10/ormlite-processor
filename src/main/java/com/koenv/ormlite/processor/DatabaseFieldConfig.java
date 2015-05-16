/*
 * This document is part of the ORMLite project.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that this permission
 * notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
 * CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * The author may be contacted via http://ormlite.com/
 */
package com.koenv.ormlite.processor;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;

/**
 * Database field configuration information either supplied by a {@link DatabaseField} annotation or by direct Java or
 * Spring wiring.
 *
 * @author graywatson
 */
public class DatabaseFieldConfig {

    private static final int DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL = ForeignCollectionField.MAX_EAGER_LEVEL;
    public static final DataType DEFAULT_DATA_TYPE = DataType.UNKNOWN;
    public static final boolean DEFAULT_CAN_BE_NULL = true;
    public static final boolean DEFAULT_FOREIGN_COLLECTION_ORDER_ASCENDING = true;

    private String fieldName;
    private String columnName;
    private DataType dataType = DEFAULT_DATA_TYPE;
    private String defaultValue;
    private int width;
    private boolean canBeNull = DEFAULT_CAN_BE_NULL;
    private boolean id;
    private boolean generatedId;
    private String generatedIdSequence;
    private boolean foreign;
    private boolean useGetSet;
    private Element unknownEnumValue;
    private boolean throwIfNull;
    private String format;
    private boolean unique;
    private boolean uniqueCombo;
    private boolean index;
    private String indexName;
    private boolean uniqueIndex;
    private String uniqueIndexName;
    private boolean foreignAutoRefresh;
    private int maxForeignAutoRefreshLevel = DatabaseField.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED;
    private TypeElement persisterClass = null;
    private boolean allowGeneratedIdInsert;
    private String columnDefinition;
    private boolean foreignAutoCreate;
    private boolean version;
    private String foreignColumnName;
    private boolean readOnly;
    // foreign collection field information
    private boolean foreignCollection;
    private boolean foreignCollectionEager;
    private int foreignCollectionMaxEagerLevel = DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL;
    private String foreignCollectionColumnName;
    private String foreignCollectionOrderColumnName;
    private boolean foreignCollectionOrderAscending = DEFAULT_FOREIGN_COLLECTION_ORDER_ASCENDING;
    private String foreignCollectionForeignFieldName;

    public DatabaseFieldConfig() {
    }

    /**
     * Return the name of the field in the class.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @see DatabaseField#columnName()
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @see DatabaseField#dataType()
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * @see DatabaseField#defaultValue()
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @see DatabaseField#width()
     */
    public int getWidth() {
        return width;
    }

    /**
     * @see DatabaseField#canBeNull()
     */
    public boolean isCanBeNull() {
        return canBeNull;
    }

    /**
     * @see DatabaseField#id()
     */
    public boolean isId() {
        return id;
    }

    /**
     * @see DatabaseField#generatedId()
     */
    public boolean isGeneratedId() {
        return generatedId;
    }

    /**
     * @see DatabaseField#generatedIdSequence()
     */
    public String getGeneratedIdSequence() {
        return generatedIdSequence;
    }

    /**
     * @see DatabaseField#foreign()
     */
    public boolean isForeign() {
        return foreign;
    }

    /**
     * @see DatabaseField#useGetSet()
     */
    public boolean isUseGetSet() {
        return useGetSet;
    }

    public Element getUnknownEnumValue() {
        return unknownEnumValue;
    }

    public boolean isThrowIfNull() {
        return throwIfNull;
    }

    public String getFormat() {
        return format;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isUniqueCombo() {
        return uniqueCombo;
    }

    public String getIndexName(String tableName) {
        if (index && indexName == null) {
            indexName = findIndexName(tableName);
        }
        return indexName;
    }

    public String getUniqueIndexName(String tableName) {
        if (uniqueIndex && uniqueIndexName == null) {
            uniqueIndexName = findIndexName(tableName);
        }
        return uniqueIndexName;
    }

    public boolean isForeignAutoRefresh() {
        return foreignAutoRefresh;
    }

    public int getMaxForeignAutoRefreshLevel() {
        return maxForeignAutoRefreshLevel;
    }

	/*
     * Foreign collection field configurations
	 */

    public boolean isForeignCollection() {
        return foreignCollection;
    }

    public boolean isForeignCollectionEager() {
        return foreignCollectionEager;
    }

    public int getForeignCollectionMaxEagerLevel() {
        return foreignCollectionMaxEagerLevel;
    }

    public String getForeignCollectionColumnName() {
        return foreignCollectionColumnName;
    }

    public String getForeignCollectionOrderColumnName() {
        return foreignCollectionOrderColumnName;
    }

    public boolean isForeignCollectionOrderAscending() {
        return foreignCollectionOrderAscending;
    }

    public String getForeignCollectionForeignFieldName() {
        return foreignCollectionForeignFieldName;
    }

    public TypeElement getPersisterClass() {
        return persisterClass;
    }

    public boolean isAllowGeneratedIdInsert() {
        return allowGeneratedIdInsert;
    }

    public String getColumnDefinition() {
        return columnDefinition;
    }

    public boolean isForeignAutoCreate() {
        return foreignAutoCreate;
    }

    public boolean isVersion() {
        return version;
    }

    public String getForeignColumnName() {
        return foreignColumnName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public static DatabaseFieldConfig fromDatabaseField(DatabaseType databaseType, Element field, DatabaseField databaseField, Types typeUtils) {
        DatabaseFieldConfig config = new DatabaseFieldConfig();
        config.fieldName = field.getSimpleName().toString();
        if (databaseType.isEntityNamesMustBeUpCase()) {
            config.fieldName = config.fieldName.toUpperCase();
        }
        config.columnName = valueIfNotBlank(databaseField.columnName());
        config.dataType = databaseField.dataType();
        // NOTE: == did not work with the NO_DEFAULT string
        String defaultValue = databaseField.defaultValue();
        if (!defaultValue.equals(DatabaseField.DEFAULT_STRING)) {
            config.defaultValue = defaultValue;
        }
        config.width = databaseField.width();
        config.canBeNull = databaseField.canBeNull();
        config.id = databaseField.id();
        config.generatedId = databaseField.generatedId();
        config.generatedIdSequence = valueIfNotBlank(databaseField.generatedIdSequence());
        config.foreign = databaseField.foreign();
        config.useGetSet = databaseField.useGetSet();
        config.unknownEnumValue = findMatchingEnumVal(field, databaseField.unknownEnumName());
        config.throwIfNull = databaseField.throwIfNull();
        config.format = valueIfNotBlank(databaseField.format());
        config.unique = databaseField.unique();
        config.uniqueCombo = databaseField.uniqueCombo();
        // add in the index information
        config.index = databaseField.index();
        config.indexName = valueIfNotBlank(databaseField.indexName());
        config.uniqueIndex = databaseField.uniqueIndex();
        config.uniqueIndexName = valueIfNotBlank(databaseField.uniqueIndexName());
        config.foreignAutoRefresh = databaseField.foreignAutoRefresh();
        config.maxForeignAutoRefreshLevel = databaseField.maxForeignAutoRefreshLevel();
        try {
            databaseField.persisterClass();
        } catch (MirroredTypeException e) {
            Element element = typeUtils.asElement(e.getTypeMirror());
            if (!element.getKind().equals(ElementKind.CLASS)) {
                throw new IllegalStateException("persisterClass must be a class.");
            }
            config.persisterClass = (TypeElement) element;
        }
        config.allowGeneratedIdInsert = databaseField.allowGeneratedIdInsert();
        config.columnDefinition = valueIfNotBlank(databaseField.columnDefinition());
        config.foreignAutoCreate = databaseField.foreignAutoCreate();
        config.version = databaseField.version();
        config.foreignColumnName = valueIfNotBlank(databaseField.foreignColumnName());
        config.readOnly = databaseField.readOnly();

        return config;
    }

    /**
     * Internal method that finds the matching enum for a configured field that has the name argument.
     *
     * @return The matching enum value or null if blank enum name.
     * @throws IllegalArgumentException If the enum name is not known.
     */
    public static Element findMatchingEnumVal(Element field, String unknownEnumName) {
        if (unknownEnumName == null || unknownEnumName.length() == 0) {
            return null;
        }
        if (!field.getKind().equals(ElementKind.ENUM)) {
            throw new IllegalStateException("Element must be an enum to be used with unknownEnumName.");
        }
        for (Element element : field.getEnclosedElements()) {
            if (element.getKind().equals(ElementKind.ENUM_CONSTANT)) {
                if (element.getSimpleName().toString().equals(unknownEnumName)) {
                    return element;
                }
            }
        }
        throw new IllegalArgumentException("Unknwown enum unknown name " + unknownEnumName + " for field " + field);
    }

    public static DatabaseFieldConfig fromForeignCollection(Element field, ForeignCollectionField foreignCollection) {
        DatabaseFieldConfig config = new DatabaseFieldConfig();
        config.fieldName = field.getSimpleName().toString();
        if (foreignCollection.columnName().length() > 0) {
            config.columnName = foreignCollection.columnName();
        }
        config.foreignCollection = true;
        config.foreignCollectionEager = foreignCollection.eager();
        @SuppressWarnings("deprecation")
        int maxEagerLevel = foreignCollection.maxEagerForeignCollectionLevel();
        if (maxEagerLevel != ForeignCollectionField.MAX_EAGER_LEVEL) {
            config.foreignCollectionMaxEagerLevel = maxEagerLevel;
        } else {
            config.foreignCollectionMaxEagerLevel = foreignCollection.maxEagerLevel();
        }
        config.foreignCollectionOrderColumnName = valueIfNotBlank(foreignCollection.orderColumnName());
        config.foreignCollectionOrderAscending = foreignCollection.orderAscending();
        config.foreignCollectionColumnName = valueIfNotBlank(foreignCollection.columnName());
        String foreignFieldName = valueIfNotBlank(foreignCollection.foreignFieldName());
        if (foreignFieldName == null) {
            @SuppressWarnings("deprecation")
            String foreignColumnName = valueIfNotBlank(foreignCollection.foreignColumnName());
            config.foreignCollectionForeignFieldName = valueIfNotBlank(foreignColumnName);
        } else {
            config.foreignCollectionForeignFieldName = foreignFieldName;
        }
        return config;
    }

    private String findIndexName(String tableName) {
        if (columnName == null) {
            return tableName + "_" + fieldName + "_idx";
        } else {
            return tableName + "_" + columnName + "_idx";
        }
    }

    private static String valueIfNotBlank(String newValue) {
        if (newValue == null || newValue.length() == 0) {
            return null;
        } else {
            return newValue;
        }
    }
}
