import java.util.Random;

public class SkipListTimeTest {
  public static void main(String[] args) {
    int[] timeToTest = new int[]{100, 1000, 10000, 100000, 1000000};
    System.out.printf("          %8s%8s%8s\n", "set", "get", "remove");

    for (int i = 0; i < timeToTest.length; i++) {
      long[] times = test(timeToTest[i]);
      System.out.printf("%10d%8d%8d%8d\n", timeToTest[i], times[0], times[1], times[2]);
    }
  }

  private static long[] test(int size) {
    SkipList<Integer, Integer> list = new SkipList<>();
    int[] num = new int[size];
    for (int i = 0; i < num.length; i++) {
      num[i] = i;
    }
    shuffle(num);

    /* [setTime, getTime, removeTime] */
    long[] elapsedTime = new long[3];
    long startTime, endTime;

    /* setTest */
    startTime = System.nanoTime();
    for (int i = 0; i < num.length; i++) {
      list.set(num[i], num[i]);
    }
    endTime = System.nanoTime();

    elapsedTime[0] = (endTime - startTime) / 1000000; // convert it to milliseconds

    /* getTest */
    startTime = System.nanoTime();
    for (int i = 0; i < num.length; i++) {
      list.get(num[i]);
    }
    endTime = System.nanoTime();

    elapsedTime[1] = (endTime - startTime) / 1000000; // convert it to milliseconds

    /* removeTest */
    startTime = System.nanoTime();
    for (int i = 0; i < num.length; i++) {
      list.remove(num[i]);
    }
    endTime = System.nanoTime();

    elapsedTime[2] = (endTime - startTime) / 1000000; // convert it to milliseconds

    return elapsedTime;
  }

  private static void shuffle(int[] arr) {
    Random rand = new Random();
    for (int i = arr.length - 1; i >= 0; i--) {
      swap(arr, i, rand.nextInt(i + 1));
    }
  }

  private static void swap(int[] arr, int index1, int index2) {
    if (index1 == index2) {
      return;
    }
    int temp = arr[index1];
    arr[index1] = arr[index2];
    arr[index2] = temp;
  }
}
