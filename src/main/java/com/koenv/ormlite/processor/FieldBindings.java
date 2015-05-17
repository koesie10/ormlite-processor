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

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Database field configuration information either supplied by a {@link DatabaseField} annotation or by direct Java or
 * Spring wiring.
 *
 * @author graywatson
 */
public class FieldBindings {

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

    public FieldBindings() {
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

    public static FieldBindings fromDatabaseField(DatabaseType databaseType, Element field, DatabaseField databaseField, Types typeUtils, Messager messager) {
        FieldBindings bindings = new FieldBindings();
        bindings.fieldName = field.getSimpleName().toString();
        if (databaseType.isEntityNamesMustBeUpCase()) {
            bindings.fieldName = bindings.fieldName.toUpperCase();
        }
        bindings.columnName = valueIfNotBlank(databaseField.columnName());
        bindings.dataType = databaseField.dataType();
        // NOTE: == did not work with the NO_DEFAULT string
        String defaultValue = databaseField.defaultValue();
        if (!defaultValue.equals(DatabaseField.DEFAULT_STRING)) {
            bindings.defaultValue = defaultValue;
        }
        bindings.width = databaseField.width();
        bindings.canBeNull = databaseField.canBeNull();
        bindings.id = databaseField.id();
        bindings.generatedId = databaseField.generatedId();
        bindings.generatedIdSequence = valueIfNotBlank(databaseField.generatedIdSequence());
        bindings.foreign = databaseField.foreign();
        bindings.useGetSet = databaseField.useGetSet();
        bindings.unknownEnumValue = findMatchingEnumVal(field, databaseField.unknownEnumName(), messager);
        bindings.throwIfNull = databaseField.throwIfNull();
        bindings.format = valueIfNotBlank(databaseField.format());
        bindings.unique = databaseField.unique();
        bindings.uniqueCombo = databaseField.uniqueCombo();
        // add in the index information
        bindings.index = databaseField.index();
        bindings.indexName = valueIfNotBlank(databaseField.indexName());
        bindings.uniqueIndex = databaseField.uniqueIndex();
        bindings.uniqueIndexName = valueIfNotBlank(databaseField.uniqueIndexName());
        bindings.foreignAutoRefresh = databaseField.foreignAutoRefresh();
        bindings.maxForeignAutoRefreshLevel = databaseField.maxForeignAutoRefreshLevel();
        try {
            databaseField.persisterClass();
        } catch (MirroredTypeException e) {
            Element element = typeUtils.asElement(e.getTypeMirror());
            if (!element.getKind().equals(ElementKind.CLASS)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "persisterClass must be a class", element);
                return null;
            }
            bindings.persisterClass = (TypeElement) element;
        }
        bindings.allowGeneratedIdInsert = databaseField.allowGeneratedIdInsert();
        bindings.columnDefinition = valueIfNotBlank(databaseField.columnDefinition());
        bindings.foreignAutoCreate = databaseField.foreignAutoCreate();
        bindings.version = databaseField.version();
        bindings.foreignColumnName = valueIfNotBlank(databaseField.foreignColumnName());
        bindings.readOnly = databaseField.readOnly();

        return bindings;
    }

    /**
     * Internal method that finds the matching enum for a configured field that has the name argument.
     *
     * @return The matching enum value or null if blank enum name.
     */
    public static Element findMatchingEnumVal(Element field, String unknownEnumName, Messager messager) {
        if (unknownEnumName == null || unknownEnumName.length() == 0) {
            return null;
        }
        VariableElement variableElement = (VariableElement) field;
        DeclaredType type = (DeclaredType) variableElement.asType();
        Element declaredElement = type.asElement();
        if (!declaredElement.getKind().equals(ElementKind.ENUM)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "To use unknownEnumValue, the parameter must be of type enum", declaredElement);
        }
        TypeElement typeElement = (TypeElement) declaredElement;
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind().equals(ElementKind.ENUM_CONSTANT)) {
                if (element.getSimpleName().toString().equals(unknownEnumName)) {
                    return element;
                }
            }
        }
        messager.printMessage(Diagnostic.Kind.ERROR, "Unknown enum unknown name: " + unknownEnumName + " for field " + field);
        return null;
    }

    public static FieldBindings fromForeignCollection(Element field, ForeignCollectionField foreignCollection) {
        FieldBindings bindings = new FieldBindings();
        bindings.fieldName = field.getSimpleName().toString();
        if (foreignCollection.columnName().length() > 0) {
            bindings.columnName = foreignCollection.columnName();
        }
        bindings.foreignCollection = true;
        bindings.foreignCollectionEager = foreignCollection.eager();
        @SuppressWarnings("deprecation")
        int maxEagerLevel = foreignCollection.maxEagerForeignCollectionLevel();
        if (maxEagerLevel != ForeignCollectionField.MAX_EAGER_LEVEL) {
            bindings.foreignCollectionMaxEagerLevel = maxEagerLevel;
        } else {
            bindings.foreignCollectionMaxEagerLevel = foreignCollection.maxEagerLevel();
        }
        bindings.foreignCollectionOrderColumnName = valueIfNotBlank(foreignCollection.orderColumnName());
        bindings.foreignCollectionOrderAscending = foreignCollection.orderAscending();
        bindings.foreignCollectionColumnName = valueIfNotBlank(foreignCollection.columnName());
        String foreignFieldName = valueIfNotBlank(foreignCollection.foreignFieldName());
        if (foreignFieldName == null) {
            @SuppressWarnings("deprecation")
            String foreignColumnName = valueIfNotBlank(foreignCollection.foreignColumnName());
            bindings.foreignCollectionForeignFieldName = valueIfNotBlank(foreignColumnName);
        } else {
            bindings.foreignCollectionForeignFieldName = foreignFieldName;
        }
        return bindings;
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
