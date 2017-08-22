import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * 
 * Calculates the longest sequence of a given user number in a array. This is
 * done by using parallelism.
 *
 */
public class LongestSequence {
	public static final ForkJoinPool POOL = new ForkJoinPool();

	public static SequenceRange sequential(int val, int[] arr, int sequentialCutoff, int low, int high) {
		SequenceRange result = new SequenceRange(0, 0, 0, high - low);
		int leftMatchingCount = 0;
		int index1 = low;
		while (index1 < high && arr[index1] == val) {
			leftMatchingCount++;
			index1++;
		}
		int rightMatchingCount = 0;
		int index2 = high - 1;
		while (index2 >= low && arr[index2] == val) {
			rightMatchingCount++;
			index2--;
		}
		int longestSequence = 0;
		int sequenceLength = 0;
		for (int i = low; i < high; i++) {
			if (arr[i] == val) {
				sequenceLength++;
			} else if (sequenceLength > longestSequence) {
				longestSequence = sequenceLength;
				sequenceLength = 0;
			} else {
				sequenceLength = 0;
			}
		}
		if (sequenceLength > longestSequence) {
			longestSequence = sequenceLength;
		}
		result.matchingOnLeft = leftMatchingCount;
		result.matchingOnRight = rightMatchingCount;
		result.longestRange = longestSequence;
		return result;
	}

	public static int getLongestSequence(int val, int[] arr, int sequentialCutoff) {
		SequenceRange result = POOL.invoke(new LongestSequenceTask(val, arr, sequentialCutoff, 0, arr.length));
		return result.longestRange;
	}

	static class LongestSequenceTask extends RecursiveTask<SequenceRange> {
		int val;
		int[] arr;
		int sequentialCutoff;
		int low, high;

		public LongestSequenceTask(int val, int[] arr, int sequentialCutoff, int low, int high) {
			this.val = val;
			this.arr = arr;
			this.sequentialCutoff = sequentialCutoff;
			this.low = low;
			this.high = high;
		}

		protected SequenceRange compute() {
			if (high - low <= sequentialCutoff) {
				return sequential(val, arr, sequentialCutoff, low, high);
			}
			int mid = low + (high - low) / 2;
			LongestSequenceTask left = new LongestSequenceTask(val, arr, sequentialCutoff, low, mid);
			LongestSequenceTask right = new LongestSequenceTask(val, arr, sequentialCutoff, mid, high);
			left.fork();
			SequenceRange rightResult = right.compute();
			SequenceRange leftResult = left.join();
			int longest = Math.max(leftResult.matchingOnRight + rightResult.matchingOnLeft,
					Math.max(leftResult.longestRange, rightResult.longestRange));
			int matchingOnLeft = leftResult.matchingOnLeft;
			int matchingOnRight = rightResult.matchingOnRight;
			if (matchingOnLeft == leftResult.sequenceLength) {
				matchingOnLeft = matchingOnLeft + rightResult.matchingOnLeft;
			}
			if (matchingOnRight == rightResult.sequenceLength) {
				matchingOnRight = matchingOnRight + leftResult.matchingOnRight;
			}
			rightResult.longestRange = longest;
			rightResult.sequenceLength = leftResult.sequenceLength + rightResult.sequenceLength;
			rightResult.matchingOnRight = matchingOnRight;
			rightResult.matchingOnLeft = matchingOnLeft;
			return rightResult;
		}
	}

	public static void main(String[] args) {
		int[] array = new int[1000];
		Random rand = new Random();
		for (int i = 0; i < array.length; i++) {
			array[i] = rand.nextInt(11);
		}
		Scanner console = new Scanner(System.in);
		System.out.print("Enter a integer from 0 to 10: ");
		int value = console.nextInt();
		System.out.println(Arrays.toString(array));
		System.out.println("The longest sequence for " + value + " is " + getLongestSequence(4, array, value));
	}
}