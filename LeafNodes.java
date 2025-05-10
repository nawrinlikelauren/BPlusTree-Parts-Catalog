import java.util.*;

public class LeafNodes extends Nodes {
    public static final int MAX_LEAF_NODE = 16;

    public int numberOfEntry;
    public LeafNodes leftSibling;
    public LeafNodes rightSibling;
    public Structure[] structures;

    public LeafNodes(Structure structure) {
        super();
        this.structures = new Structure[MAX_LEAF_NODE];
        this.numberOfEntry = 0;
        this.insertProduct(structure);
    }

    public LeafNodes(Structure[] structures, BPlusIndex parent) {
        super();
        this.structures = structures;
        this.numberOfEntry = searchEmpty(structures);
        this.parent = parent;
    }

    public void deleteProduct(int index) {
        //System.out.println(Arrays.toString(structures));
        structures[index] = null;
        //System.out.println(Arrays.toString(structures));
        numberOfEntry--;
    }

    public boolean insertProduct(Structure structure) {
        if (this.isFull()) {
            return false;
        } else {
            this.structures[numberOfEntry] = structure;
            numberOfEntry++;
            Arrays.sort(this.structures, 0, numberOfEntry);
            return true;
        }
    }

    public boolean hasLowerThenMinimum() {
        return numberOfEntry < MAX_LEAF_NODE;
    }

    public boolean isFull() {
        return numberOfEntry == MAX_LEAF_NODE - 1;
    }

    public boolean canLend() {
        return numberOfEntry > MAX_LEAF_NODE;
    }

    public boolean canMerge() {
        return numberOfEntry == MAX_LEAF_NODE;
    }

    private int searchEmpty(Structure[] structures) {
        for (int i = 0; i < structures.length; i++) {
            if (structures[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
