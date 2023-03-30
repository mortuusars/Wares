package io.github.mortuusars.wares.test;

import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.wares.Wares;
import io.github.mortuusars.wares.test.data.agreement.AgreementTest;
import io.github.mortuusars.wares.test.framework.Test;
import io.github.mortuusars.wares.test.framework.TestResult;
import io.github.mortuusars.wares.test.framework.TestingResult;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Tests {

    private ServerPlayer player;

    public Tests(ServerPlayer player) {
        this.player = player;
    }

    public TestingResult run() {
        Wares.LOGGER.info("RUNNING TESTS");

        Pair<List<TestResult>, List<TestResult>> ran = run(new AgreementTest().collect());
        List<TestResult> skipped = skip();
        TestingResult testingResult = new TestingResult(ran.getFirst(), ran.getSecond(), skipped);
        Wares.LOGGER.info(String.join("",
                "TESTS COMPLETED!\n",
                testingResult.getTotalTestCount() + " test(s) were conducted.",
                testingResult.passed().size() > 0 ? ("\nPassed:\n" + String.join("\n", testingResult.passed().stream()
                        .map(TestResult::toString).collect(Collectors.toList()))) : "",
                testingResult.failed().size() > 0 ? ("\nFailed:\n" + String.join("\n", testingResult.failed().stream()
                        .map(TestResult::toString).collect(Collectors.toList()))) : "",
                testingResult.skipped().size() > 0 ? ("\nSkipped:\n" + String.join("\n", testingResult.skipped().stream()
                        .map(TestResult::toString).collect(Collectors.toList()))) : ""));
        return testingResult;
    }

    @SafeVarargs
    private Pair<List<TestResult>, List<TestResult>> run(List<Test>... tests) {
        List<TestResult> passed = new ArrayList<>();
        List<TestResult> failed = new ArrayList<>();
        for (List<Test> list : tests) {
            for (Test test : list) {
                TestResult testResult = runTest(test);
                if (testResult.status() == TestResult.Status.PASSED)
                    passed.add(testResult);
                else
                    failed.add(testResult);
            }
        }
        return Pair.of(passed, failed);
    }

    @SafeVarargs
    private List<TestResult> skip(List<Test>... tests) {
        List<TestResult> results = new ArrayList<>();
        for (List<Test> list : tests) {
            for (Test test : list) {
                results.add(TestResult.skip(test.name));
            }
        }
        return results;
    }

    private TestResult runTest(Test test) {
        try {
            test.test.accept(player);
            return TestResult.pass(test.name);
        }
        catch (Exception e) {
//            Wares.LOGGER.error("Failed test: '" + test.name + "', Error: " + e);
            return TestResult.error(test.name, e.toString());
        }
    }
}
