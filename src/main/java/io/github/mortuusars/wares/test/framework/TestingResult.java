package io.github.mortuusars.wares.test.framework;

import java.util.List;

public record TestingResult(List<TestResult> passed, List<TestResult> failed, List<TestResult> skipped) {
    public int getTotalTestCount() {
        return passed.size() + failed.size() + skipped.size();
    }
}
