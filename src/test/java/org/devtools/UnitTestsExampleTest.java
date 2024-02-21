package org.devtools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UnitTestsExampleTest {

    private UnitTestsExample unitTestsExample;

    @BeforeEach
    void init() {
        unitTestsExample = new UnitTestsExample();
    }

    @Test
    void extractNumbersAfterLastFour() {
        int[] arr =  {1, 2, 3, 4, 5, 6, 7};
        int[] result = {5, 6, 7};
        assertArrayEquals(result, unitTestsExample.extractNumbersAfterLastFour(arr));
    }

    @MethodSource("addArrays")
    @ParameterizedTest
    void massExtractNumbersAfterLastFour(int[] arr, int [] result) {
        assertArrayEquals(result, unitTestsExample.extractNumbersAfterLastFour(arr));
    }

    public static Stream<Arguments> addArrays() {
        List<Arguments> args = new ArrayList<>();
        int[] arr = new int[] {1, 4, 3, 4, 5, 6, 7};
        int[] result = new int[] {5, 6, 7};
        args.add(Arguments.of(arr, result));
        args.add(Arguments.of(new int[] {4, 4, 3, 4, 1, 4, 7}, new int[] {7}));
        args.add(Arguments.of(new int[] {4, 4, 4, 4, 1, 2, 3}, new int[] {1, 2, 3}));
        args.add(Arguments.of(new int[] {1, 2, 4, 5, 1, 2, 4}, null));
        return args.stream();
    }

    @Test
    void shouldThrowRuntimeExceptionWhenDoesNotContainFour() {
        assertThrows(RuntimeException.class, () -> unitTestsExample.extractNumbersAfterLastFour(new int[] {1, 2, 3, 5}));
    }

    @MethodSource("addAnotherArrays")
    @ParameterizedTest
    void massIsContainsOneOrFour(int[] arr, boolean result) {
        assertEquals(result, unitTestsExample.isContainsOneOrFour(arr));
    }

    public static Stream<Arguments> addAnotherArrays() {
        List<Arguments> args = new ArrayList<>();
        int[] a = null;
        args.add(Arguments.of(new int[] {1, 1, 3, 6, 1, 4, 8}, true));
        args.add(Arguments.of(new int[] {4, 4, 4, 4, 1, 2, 3}, true));
        args.add(Arguments.of(new int[] {4, 5, 3, 10, 4}, true));
        args.add(Arguments.of(new int[] {2, 2, 4}, true));
        args.add(Arguments.of(new int[] {5, 12, 7, 3, 8, 2, 3}, false));
        return args.stream();
    }
}