
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple, dependency-free Test Runner.
 * Executes methods starting with "test" in the provided class.
 */
public class TestRunner {

    public static void runTests(Class<?> testClass) {
        System.out.println("Running tests for: " + testClass.getSimpleName());
        System.out.println("--------------------------------------------------");

        int passed = 0;
        int failed = 0;
        List<String> failedTests = new ArrayList<>();

        try {
            Object instance = testClass.getDeclaredConstructor().newInstance();
            Method[] methods = testClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().startsWith("test")) {
                    System.out.print("Running " + method.getName() + "... ");
                    try {
                        method.invoke(instance);
                        System.out.println("PASSED");
                        passed++;
                    } catch (Exception e) {
                        System.out.println("FAILED");
                        e.printStackTrace(); // Print the full stack trace for debugging
                        failed++;
                        failedTests.add(method.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Summary: " + (passed + failed) + " run, " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.out.println("Failed tests: " + failedTests);
            System.exit(1); // Exit with error code if any test failed
        }
    }

    // Assertion helpers
    public static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new RuntimeException("Assertion failed: Expected " + expected + " but got " + actual);
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException("Assertion failed: " + message);
        }
    }
}
