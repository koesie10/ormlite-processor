/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Koen Vlaswinkel
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

import com.google.common.base.Joiner;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.j256.ormlite.field.DatabaseFieldConfig.DEFAULT_DATA_TYPE;

public class AnnotationProcessor extends AbstractProcessor {
    private static final int DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL = ForeignCollectionField.MAX_EAGER_LEVEL;

    private Types typeUtils;
    private Filer filer;
    private Messager messager;

    private static final DatabaseType databaseType = new SqliteAndroidDatabaseType();

    private List<ClassName> generatedClasses;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        generatedClasses = new ArrayList<ClassName>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(DatabaseTable.class)) {
            if (!annotatedElement.getKind().isClass()) {
                error(annotatedElement, "Only classes can be annotated with %s", DatabaseTable.class.getSimpleName());
                return false;
            }
            TypeElement typeElement = (TypeElement) annotatedElement;
            String tableName = extractTableName(typeElement);
            List<FieldBindings> fieldConfigs = new ArrayList<FieldBindings>();
            // walk up the classes finding the fields
            TypeElement working = typeElement;
            while (working != null) {
                for (Element element : working.getEnclosedElements()) {
                    if (element.getKind().isField()) {
                        if (element.getAnnotation(DatabaseField.class) != null) {
                            DatabaseField databaseField = element.getAnnotation(DatabaseField.class);
                            if (!databaseField.persisted()) {
                                continue;
                            }
                            FieldBindings fieldConfig = FieldBindings.fromDatabaseField(databaseType, element, databaseField, typeUtils, messager);
                            if (fieldConfig != null) {
                                fieldConfigs.add(fieldConfig);
                            }
                        } else if (element.getAnnotation(ForeignCollectionField.class) != null) {
                            ForeignCollectionField foreignCollectionField = element.getAnnotation(ForeignCollectionField.class);
                            FieldBindings fieldConfig = FieldBindings.fromForeignCollection(element, foreignCollectionField);
                            if (fieldConfig != null) {
                                fieldConfigs.add(fieldConfig);
                            }
                        }
                    }
                }
                if (working.getSuperclass().getKind().equals(TypeKind.NONE)) {
                    break;
                }
                working = (TypeElement) typeUtils.asElement(working.getSuperclass());
            }
            if (fieldConfigs.isEmpty()) {
                error(
                        typeElement,
                        "Every class annnotated with %s must have at least 1 field annotated with %s",
                        DatabaseTable.class.getSimpleName(),
                        DatabaseField.class.getSimpleName()
                );
                return false;
            }
            JavaFile javaFile = generateFile(typeElement, fieldConfigs, tableName);
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "Code gen failed: " + e);
                return false;
            }
        }

        if (!generatedClasses.isEmpty()) {
            JavaFile javaFile = generateMainFile();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Code gen failed: failed to generate main class: " + e);
                return false;
            }
        }

        return false;
    }

    private JavaFile generateMainFile() {
        ClassName className = ClassName.get("com.koenv.ormlite.processor", "OrmLiteProcessor");

        TypeSpec.Builder configBuilder = TypeSpec.classBuilder(className.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Generated on $L\n", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date()));

        ParameterizedTypeName databaseTableConfig = ParameterizedTypeName.get(ClassName.get(DatabaseTableConfig.class), WildcardTypeName.subtypeOf(Object.class));

        ParameterizedTypeName collectionOfTableConfigs = ParameterizedTypeName.get(ClassName.get(Collection.class), databaseTableConfig);
        ParameterizedTypeName listOfTableConfigs = ParameterizedTypeName.get(ClassName.get(ArrayList.class), databaseTableConfig);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addJavadoc("Call this method when initializing your application\n")
                .addStatement("$T configs = new $T()", collectionOfTableConfigs, listOfTableConfigs);

        for (ClassName tableConfig : generatedClasses) {
            methodBuilder.addStatement("configs.add($T.getTableConfig())", tableConfig);
        }

        methodBuilder.addStatement("$T.addCachedDatabaseConfigs(configs)", DaoManager.class);

        configBuilder.addMethod(methodBuilder.build());

        return JavaFile.builder(className.packageName(), configBuilder.build()).build();
    }

    private JavaFile generateFile(TypeElement element, List<FieldBindings> fieldConfigs, String tableName) {
        ClassName className = ClassName.get(element);
        ClassName configName = ClassName.get(className.packageName(), Joiner.on('$').join(className.simpleNames()) + "$$Configuration");

        TypeSpec.Builder configBuilder = TypeSpec.classBuilder(configName.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("Generated on $L\n", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date()));

        TypeName databaseTableConfig = ParameterizedTypeName.get(ClassName.get(DatabaseTableConfig.class), ClassName.get(element));

        MethodSpec.Builder tableConfigMethodBuilder = MethodSpec.methodBuilder("getTableConfig")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(databaseTableConfig)
                .addStatement("$T config = new $T()", databaseTableConfig, databaseTableConfig);

        tableConfigMethodBuilder.addStatement("config.setDataClass($T.class)", element);
        tableConfigMethodBuilder.addStatement("config.setTableName($S)", tableName);

        TypeName listOfFieldConfigs = ParameterizedTypeName.get(List.class, DatabaseFieldConfig.class);
        TypeName arrayListOfFieldConfigs = ParameterizedTypeName.get(ArrayList.class, DatabaseFieldConfig.class);

        MethodSpec.Builder fieldConfigsMethodBuilder = MethodSpec.methodBuilder("getFieldConfigs")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(listOfFieldConfigs)
                .addStatement("$T list = new $T()", listOfFieldConfigs, arrayListOfFieldConfigs);

        fieldConfigsMethodBuilder.addStatement("$T config = null", DatabaseFieldConfig.class);

        for (FieldBindings config : fieldConfigs) {
            fieldConfigsMethodBuilder.addCode(getFieldConfig(config, tableName));
            fieldConfigsMethodBuilder.addStatement("list.add(config)");
        }

        fieldConfigsMethodBuilder.addStatement("return list");

        MethodSpec fieldConfigsMethod = fieldConfigsMethodBuilder.build();

        configBuilder.addMethod(fieldConfigsMethod);

        tableConfigMethodBuilder.addStatement("config.setFieldConfigs($N())", fieldConfigsMethod);
        tableConfigMethodBuilder.addStatement("return config");

        configBuilder.addMethod(tableConfigMethodBuilder.build());

        generatedClasses.add(configName);

        return JavaFile.builder(configName.packageName(), configBuilder.build()).build();
    }

    private CodeBlock getFieldConfig(FieldBindings config, String tableName) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .addStatement("config = new $T()", DatabaseFieldConfig.class);
        if (config.getFieldName() != null) {
            builder.addStatement("config.setFieldName($S)", config.getFieldName());
        }
        if (config.getColumnName() != null) {
            builder.addStatement("config.setColumnName($S)", config.getColumnName());
        }
        if (config.getDataType() != DEFAULT_DATA_TYPE) {
            builder.addStatement("config.setDataType($T.$L)", config.getDataType().getClass(), config.getDataType().name());
        }
        if (config.getDefaultValue() != null) {
            builder.addStatement("config.setDefaultValue($S)", config.getDefaultValue());
        }
        if (config.getWidth() != 0) {
            builder.addStatement("config.setWidth($L)", config.getWidth());
        }
        if (!config.isCanBeNull()) {
            builder.addStatement("config.setCanBeNull($L)", config.isCanBeNull());
        }
        if (config.isId()) {
            builder.addStatement("config.setId($L)", config.isId());
        }
        if (config.isGeneratedId()) {
            builder.addStatement("config.setGeneratedId($L)", config.isGeneratedId());
        }
        if (config.getGeneratedIdSequence() != null) {
            builder.addStatement("config.setGeneratedIdSequence($S)", config.getGeneratedIdSequence());
        }
        if (config.isForeign()) {
            builder.addStatement("config.setForeign($L)", config.isForeign());
        }
        if (config.isUseGetSet()) {
            builder.addStatement("config.setUseGetSet($L)", config.isUseGetSet());
        }
        if (config.getUnknownEnumValue() != null) {
            builder.addStatement("config.setUnknownEnumValue($T.$L)", config.getUnknownEnumValue(), config.getUnknownEnumValue());
        }
        if (config.isThrowIfNull()) {
            builder.addStatement("config.setThrowIfNull($L)", config.isThrowIfNull());
        }
        if (config.getFormat() != null) {
            builder.addStatement("config.setFormat($S)", config.getFormat());
        }
        if (config.isUnique()) {
            builder.addStatement("config.setUnique($L)", config.isUnique());
        }
        if (config.isUniqueCombo()) {
            builder.addStatement("config.setUniqueCombo($L)", config.isUniqueCombo());
        }
        String indexName = config.getIndexName(tableName);
        if (indexName != null) {
            builder.addStatement("config.setIndex($L)", true);
            builder.addStatement("config.setIndexName($S)", indexName);
        }
        String uniqueIndexName = config.getUniqueIndexName(tableName);
        if (uniqueIndexName != null) {
            builder.addStatement("config.setUniqueIndex($L)", true);
            builder.addStatement("config.setUniqueIndexName($S)", uniqueIndexName);
        }
        if (config.isForeignAutoRefresh()) {
            builder.addStatement("config.setForeignAutoRefresh($L)", config.isForeignAutoRefresh());
        }
        if (config.getMaxForeignAutoRefreshLevel() != DatabaseField.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED) {
            builder.addStatement("config.setMaxForeignAutoRefreshLevel($L)", config.getMaxForeignAutoRefreshLevel());
        }
        if (config.getPersisterClass() != null && !config.getPersisterClass().getQualifiedName().toString().equals("com.j256.ormlite.field.types.VoidType")) {
            builder.addStatement("config.setPersisterClass($T.class)", config.getPersisterClass());
        }
        if (config.isAllowGeneratedIdInsert()) {
            builder.addStatement("config.setAllowGeneratedIdInsert($L)", config.isAllowGeneratedIdInsert());
        }
        if (config.getColumnDefinition() != null) {
            builder.addStatement("config.setColumnDefinition($S)", config.getColumnDefinition());
        }
        if (config.isForeignAutoCreate()) {
            builder.addStatement("config.setForeignAutoCreate($L)", config.isForeignAutoCreate());
        }
        if (config.isVersion()) {
            builder.addStatement("config.setVersion($L)", config.isVersion());
        }
        String foreignColumnName = config.getForeignColumnName();
        if (foreignColumnName != null) {
            builder.addStatement("config.setForeignColumnName($S)", config.getForeignColumnName());
        }
        if (config.isReadOnly()) {
            builder.addStatement("config.setReadOnly($L)", config.isReadOnly());
        }

		/*
         * Foreign collection settings:
		 */
        if (config.isForeignCollection()) {
            builder.addStatement("config.setForeignCollection($L)", config.isForeignCollection());
        }
        if (config.isForeignCollectionEager()) {
            builder.addStatement("config.setForeignCollectionEager($L)", config.isForeignCollectionEager());
        }
        if (config.getForeignCollectionMaxEagerLevel() != DEFAULT_MAX_EAGER_FOREIGN_COLLECTION_LEVEL) {
            builder.addStatement("config.setForeignCollectionMaxEagerLevel($L)", config.getForeignCollectionMaxEagerLevel());
        }
        if (config.getForeignCollectionColumnName() != null) {
            builder.addStatement("config.setForeignCollectionColumnName($S)", config.getForeignCollectionColumnName());
        }
        if (config.getForeignCollectionOrderColumnName() != null) {
            builder.addStatement("config.setForeignCollectionOrderColumnName($S)", config.getForeignCollectionOrderColumnName());
        }
        if (!config.isForeignCollectionOrderAscending()) {
            builder.addStatement("config.setForeignCollectionOrderAscending($L)", config.isForeignCollectionOrderAscending());
        }
        if (config.getForeignCollectionForeignFieldName() != null) {
            builder.addStatement("config.setForeignCollectionForeignFieldName($S)", config.getForeignCollectionForeignFieldName());
        }

        return builder.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(DatabaseTable.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e
        );
    }

    private static String extractTableName(TypeElement element) {
        DatabaseTable databaseTable = element.getAnnotation(DatabaseTable.class);
        String name;
        if (databaseTable != null && databaseTable.tableName() != null && databaseTable.tableName().length() > 0) {
            name = databaseTable.tableName();
        } else {
            // if the name isn't specified, it is the class name lowercased
            name = element.getSimpleName().toString().toLowerCase();
        }
        return name;
    }
}
