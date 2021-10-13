package origami.folding.permutation;

/**
 * Author: Mu-Tsun Tsai
 * 
 * This is a much more efficient permutation generator than the original
 * implementation by Mr.Meguro. It uses the classical digit swapping idea to
 * reduce half of the work searching for next available element. It is also
 * equipped with an improved PairGuide to help skipping vast amount of
 * permutations that won't work.
 */
public class PermutationGenerator {

    private int count = 0;
    private final int numDigits;

    /** digits[i] gives the element at position i. */
    private final int[] digits;

    /** map[i] gives the position of element i. */
    private final int[] map;

    // swapHistory[i] == j >= i means we swap index i with index j in step i;
    // swapHistory[i] == i - 1 means we're not done yet.
    private final int[] swapHistory;

    private final PairGuide pairGuide;

    // An initial permutation, where the lock sequence return by PairGuide is
    // located at the end.
    private final int[] initPermutation;

    // If an element is in the lock sequence.
    private final boolean[] isLocked;

    private int lockCount;
    private int lockRemain;

    public PermutationGenerator(int numDigits) {
        this.numDigits = numDigits;
        this.digits = new int[numDigits + 1];
        this.initPermutation = new int[numDigits + 1];
        this.isLocked = new boolean[numDigits + 1];
        this.map = new int[numDigits + 1];
        this.swapHistory = new int[numDigits + 1];
        this.pairGuide = new PairGuide(numDigits);
    }

    public void reset() {
        count = 0;
        lockRemain = lockCount;
        for (int i = 1; i <= numDigits; i++) {
            digits[i] = initPermutation[i];
            map[i] = i;
            swapHistory[i] = i - 1;
        }
        pairGuide.reset();
        next(0);
    }

    public int next(int digit) {
        int curIndex = 1;

        // swapHistory[1] == 0 means the generator has just reset, and we don't need to
        // do anything. Otherwise we need to retract to the requested digit.
        if (swapHistory[1] != 0) {
            curIndex = numDigits;
            do {
                swapHistory[curIndex] = curIndex - 1;
                retract(--curIndex);
            } while (curIndex > digit);
        }

        while (curIndex < numDigits) {
            int swapIndex = swapHistory[curIndex];
            int curDigit = 0;

            // Find the next available element.
            do {
                swapIndex++;
                if (swapIndex > numDigits - lockRemain + 1) {
                    break;
                }
                curDigit = digits[swapIndex];
            } while (pairGuide.isNotReady(curDigit));

            // If the current digit has no available element, retract.
            if (swapIndex > numDigits - lockRemain + 1) {
                swapHistory[curIndex] = curIndex - 1;
                if (--curIndex == 0) {
                    return 0;
                }
                retract(curIndex);
                if (curIndex < digit) {
                    digit = curIndex;
                }
                continue;
            }

            // Make the swap.
            if (swapIndex != curIndex) {
                digits[swapIndex] = digits[curIndex];
                digits[curIndex] = curDigit;
            }
            swapHistory[curIndex] = swapIndex;
            map[curDigit] = curIndex;
            if (isLocked[curDigit]) {
                lockRemain--;
            }
            pairGuide.confirm(curDigit);

            curIndex++;
        }

        // Fill the last element into the map
        map[digits[numDigits]] = numDigits;

        count++;
        return digit;
    }

    public int locate(int i) {
        return map[i];
    }

    public int getCount() {
        return count;
    }

    public int getPermutation(int digit) {
        return digits[digit];
    }

    public void addGuide(int faceIndex, int upperFaceIndex) {
        pairGuide.add(faceIndex, upperFaceIndex);
    }

    public void initialize() {
        // Determine locked elements.
        int[] lock = pairGuide.lock();
        if (lock != null) {
            lockCount = lock[0];
            for (int i = 1; i <= lockCount; i++) {
                isLocked[lock[i]] = true;
            }
    
            // Prepare initial permutation.
            int i, j = 1;
            for (i = 1; i <= numDigits - lockCount; i++) {
                while (isLocked[j]) {
                    j++;
                }
                initPermutation[i] = j;
                j++;
            }
            for (i = 1; i <= lockCount; i++) {
                initPermutation[i + numDigits - lockCount] = lock[i];
            }
    
            // When generating permutations, the last locked element behaves the same as
            // normal elements.
            isLocked[lock[lockCount]] = false;
        } else {
            for (int i = 1; i <= numDigits; i++) {
                initPermutation[i] = i;
            }
        }

        reset();
    }

    private void retract(int index) {
        int swapIndex = swapHistory[index];
        int curDigit = digits[index];
        if (swapIndex != index) {
            digits[index] = digits[swapIndex];
            digits[swapIndex] = curDigit;
        }
        map[curDigit] = 0;
        if (isLocked[curDigit]) {
            lockRemain++;
        }
        pairGuide.retract(curDigit);
    }
}