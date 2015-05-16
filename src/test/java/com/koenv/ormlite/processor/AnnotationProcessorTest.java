package com.koenv.ormlite.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class AnnotationProcessorTest {
    @Test
    public void simpleTest() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("com/koenv/ormlite/processor/SimpleModel.java"))
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(
                JavaFileObjects.forResource("com/koenv/ormlite/processor/SimpleModel$$Configuration.java"),
                JavaFileObjects.forResource("com/koenv/ormlite/processor/OrmLiteProcessor.java")
        );
    }
}
