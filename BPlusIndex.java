public class BPlusIndex extends Nodes {
    public static final int MAX_ENTRY_KEY = 4;
    public static final int MIN_ENTRY_KEY = 2;

    public int numberOfEntry;
    public String[] productIDs;
    public Nodes[] indexPointers;
    public BPlusIndex leftSibling;
    public BPlusIndex rightSibling;

    public BPlusIndex(String[] productIDs) {
        this.numberOfEntry = 0;
        this.productIDs = productIDs;
        this.indexPointers = new Nodes[MAX_ENTRY_KEY + 1];
    }

    public BPlusIndex(String[] productIDs, Nodes[] indexPointers) {
        this.numberOfEntry = searchEmpty(indexPointers);
        this.productIDs = productIDs;
        this.indexPointers = indexPointers;
    }

    public void addIndexPointer(Nodes indexPointer) {
        indexPointers[numberOfEntry] = indexPointer;
        numberOfEntry++;
    }

    public int getIndexOfPointer(Nodes pointer) {
        for (int i = 0; i < indexPointers.length; i++) {
            if (indexPointers[i] == pointer) {
                return i;
            }
        }
        return -1;
    }

    public void insertPointer(Nodes pointer, int index) {
        for (int i = numberOfEntry - 1; i >= index; i--) {
            indexPointers[i + 1] = indexPointers[i];
        }
        this.indexPointers[index] = pointer;
        this.numberOfEntry++;
    }

    public boolean hasLowerThenMinimum() {
        return numberOfEntry < MIN_ENTRY_KEY;
    }

    public boolean canLend() {
        return numberOfEntry > MIN_ENTRY_KEY;
    }

    public boolean canMerge() {
        return numberOfEntry == MIN_ENTRY_KEY;
    }

    public boolean isFull() {
        return numberOfEntry == MAX_ENTRY_KEY + 1;
    }

    public void prependPointer(Nodes pointer) {
        for (int i = numberOfEntry - 1; i >= 0; i--) {
            indexPointers[i + 1] = indexPointers[i];
        }
        indexPointers[0] = pointer;
        numberOfEntry++;
    }

    public void removeKey(int index) {
        productIDs[index] = null;
    }

    public void removePointer(int index) {
        indexPointers[index] = null;
        numberOfEntry--;
    }

    public void removePointer(Nodes pointer) {
        for (int i = 0; i < indexPointers.length; i++) {
            if (indexPointers[i] == pointer) {
                indexPointers[i] = null;
                numberOfEntry--;
            }
        }
    }

    private int searchEmpty(Nodes[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
