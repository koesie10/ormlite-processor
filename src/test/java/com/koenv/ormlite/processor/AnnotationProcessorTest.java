package com.koenv.ormlite.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AnnotationProcessorTest {
    @Test
    public void simpleModelTest() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("simple_model/SimpleModel.java"))
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(
                JavaFileObjects.forResource("simple_model/SimpleModel$$Configuration.java"),
                JavaFileObjects.forResource("simple_model/OrmLiteProcessor.java")
        );
    }

    @Test
    public void enumModelTest() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("enum_model/EnumModel.java"))
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(
                JavaFileObjects.forResource("enum_model/EnumModel$$Configuration.java"),
                JavaFileObjects.forResource("enum_model/OrmLiteProcessor.java")
        );
    }

    @Test
    public void foreignCollectionModelTest() {
        assert_().about(javaSources())
                .that(Arrays.asList(
                        JavaFileObjects.forResource("foreign_collection_model/ForeignCollectionModel.java"),
                        JavaFileObjects.forResource("foreign_collection_model/ForeignCollectionModelForeign.java"))
                )
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(
                JavaFileObjects.forResource("foreign_collection_model/ForeignCollectionModel$$Configuration.java"),
                JavaFileObjects.forResource("foreign_collection_model/OrmLiteProcessor.java")
        );
    }
}
