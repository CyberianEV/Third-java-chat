package org.devtools;

public class UnitTestsExample {

    public int[] extractNumbersAfterLastFour(int[] arr) {
        int lastFourIndex = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 4) {
                lastFourIndex = i;
            }
        }
        if (lastFourIndex == -1) {
            throw new RuntimeException("there is no four in the array");
        } else if (lastFourIndex == arr.length - 1) {
            return null;
        } else {
            int resultArrLength = arr.length - lastFourIndex - 1;
            int[] result = new int[resultArrLength];
            System.arraycopy(arr, lastFourIndex + 1, result, 0, resultArrLength);
            return result;
        }
    }

    public boolean isContainsOneOrFour(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 1 || arr[i] == 4) {
                return true;
            }
        }
        return false;
    }
}
