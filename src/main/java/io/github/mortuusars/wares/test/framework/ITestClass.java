package io.github.mortuusars.wares.test.framework;

import java.util.List;

public interface ITestClass {
    List<Test> collect();


    default void assertThat(boolean condition, String error) {
        if (!condition)
            throw new IllegalStateException(error);
    }
}
