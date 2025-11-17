import java.lang.*;
import java.util.*;

public class BPlusTree {
    private BPlusIndex root;
    private LeafNodes leafHead;
    private int totalSplits;
    private int parentSplits;
    private int totalFusions;
    private int parentFusions;
    private int treeDepth;

    public BPlusTree() {
        root = null;
        leafHead = null;
        totalSplits = 0;
        totalFusions = 0;
        parentFusions = 0;
        parentSplits = 0;
        treeDepth = 0;
    }

    public ArrayList<Structure> getAllNode() {
        ArrayList<Structure> entries = new ArrayList<>();

        LeafNodes leafNode = leafHead;

        while (leafNode != null) {
            for (Structure Structure : leafNode.structures) {
                if (Structure != null) entries.add(Structure);
            }
            leafNode = leafNode.rightSibling;
        }

        return entries;
    }

    public boolean isEmpty() {
        return leafHead == null;
    }

    public ArrayList<Structure> getNextTenNode(String id) {
        if(isEmpty()){ return null; }

        LeafNodes leafNode;
        if (root == null) leafNode = leafHead;
        else leafNode = getPossibleLeafNode(id);

        Structure[] structures = leafNode.structures;
        int index = binarySearch(structures, leafNode.numberOfEntry, id);

        if(index < 0){
            return null;
        }
        else
        {
            ArrayList<Structure> result=new ArrayList<>();

            while(result.size()!=10){
                index++;
                if(index< structures.length){
                    if(structures[index]!=null){
                        result.add(structures[index]);
                    }
                }else{
                    leafNode=leafNode.rightSibling;
                    if(leafNode==null){
                        break;
                    }else{
                        // going to next leaf node
                        structures = leafNode.structures;
                        index=-1;
                    }
                }
            }

            return result;
        }
    }

    public String searchNode(String id) {

        if (isEmpty()) {
            return null;
        }
        LeafNodes leafNode;
        if (root == null) leafNode = leafHead;
        else leafNode = getPossibleLeafNode(id);

        Structure[] structures = leafNode.structures;
        int index = binarySearch(structures, leafNode.numberOfEntry, id);
        return index < 0 ? null : structures[index].description;
    }

    public void insertNode(String id, String description) {

        if (searchNode(id) != null) {
            return;
        }
        if (isEmpty()) {
            leafHead = new LeafNodes(new Structure(id, description));

        } else {

            LeafNodes leafNode;
            if (root == null) leafNode = leafHead;
            else leafNode = getPossibleLeafNode(id);

            boolean success = leafNode.insertProduct(new Structure(id, description));

            if (!success) {

                leafNode.structures[leafNode.numberOfEntry] = new Structure(id, description);
                leafNode.numberOfEntry++;
                sortEntries(leafNode.structures);
                int mid = leafNode.structures.length / 2;
                Structure[] half = splitEntries(leafNode, mid);
                if (leafNode.parent != null) {
                    String newParentId = half[0].partId;
                    leafNode.parent.productIDs[leafNode.parent.numberOfEntry - 1] = newParentId;
                    Arrays.sort(leafNode.parent.productIDs, 0, leafNode.parent.numberOfEntry);

                }
                else
                {
                    String[] parentIds = new String[BPlusIndex.MAX_ENTRY_KEY];
                    parentIds[0] = half[0].partId;
                    BPlusIndex parent = new BPlusIndex(parentIds);
                    leafNode.parent = parent;
                    parent.addIndexPointer(leafNode);
                }

                LeafNodes newLeafNode = new LeafNodes(half, leafNode.parent);

                int pointerIndex = leafNode.parent.getIndexOfPointer(leafNode) + 1;
                leafNode.parent.insertPointer(newLeafNode, pointerIndex);

                newLeafNode.rightSibling = leafNode.rightSibling;
                if (newLeafNode.rightSibling != null) {
                    newLeafNode.rightSibling.leftSibling = newLeafNode;
                }
                leafNode.rightSibling = newLeafNode;
                newLeafNode.leftSibling = leafNode;

                if (root != null) {
                    BPlusIndex in = leafNode.parent;
                    while (in != null) {
                        if (in.isFull()) {
                            splitIndexNode(in);
                        } else {
                            break;
                        }
                        in = in.parent;
                    }
                } else {
                    root = leafNode.parent;
                }
            }
        }
    }

    public boolean deleteNode(String id) {
        if (isEmpty()) {
            // tree empty
            return false;
        } else {

            LeafNodes leafNode;
            if (root == null) leafNode = leafHead;
            else leafNode = getPossibleLeafNode(id);

            int index = binarySearch(leafNode.structures, leafNode.numberOfEntry, id);

            if (index < 0) {
                return false;
            } else {

                leafNode.deleteProduct(index);

                if (leafNode.hasLowerThenMinimum()) {

                    LeafNodes sibling;
                    BPlusIndex parent = leafNode.parent;

                    if (leafNode.leftSibling != null &&
                            leafNode.leftSibling.canLend() &&
                            leafNode.leftSibling.parent == leafNode.parent) {

                        sibling = leafNode.leftSibling;
                        Structure temp = sibling.structures[sibling.numberOfEntry - 1];

                        leafNode.insertProduct(temp);
                        sortEntries(leafNode.structures);
                        sibling.deleteProduct(sibling.numberOfEntry - 1);

                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);
                        if (!(temp.partId.compareTo(parent.productIDs[pointerIndex - 1]) >= 0)) {
                            parent.productIDs[pointerIndex - 1] = leafNode.structures[0].partId;
                        }
                    } else if (leafNode.rightSibling != null &&
                            leafNode.rightSibling.canLend() &&
                            leafNode.rightSibling.parent == leafNode.parent) {

                        sibling = leafNode.rightSibling;
                        Structure temp = sibling.structures[0];

                        leafNode.insertProduct(temp);
                        sibling.deleteProduct(0);
                        sortEntries(sibling.structures);

                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);
                        if (!(temp.partId.compareTo(parent.productIDs[pointerIndex]) <= 0)) {
                            parent.productIDs[pointerIndex] = sibling.structures[0].partId;
                        }
                    } else if (leafNode.leftSibling != null &&
                            leafNode.leftSibling.canMerge() &&
                            leafNode.leftSibling.parent == leafNode.parent) {

                        sibling = leafNode.leftSibling;
                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);

                        parent.removeKey(pointerIndex - 1);
                        parent.removePointer(leafNode);

                        sibling.rightSibling = leafNode.rightSibling;

                        if (parent.hasLowerThenMinimum()) {
                            fixDeficiency(parent);
                        }

                    } else if (leafNode.rightSibling != null &&
                            leafNode.rightSibling.canMerge() &&
                            leafNode.rightSibling.parent == leafNode.parent) {


                        sibling = leafNode.rightSibling;
                        int pointerIndex = getIndexOfPointer(parent.indexPointers, leafNode);

                        parent.removeKey(pointerIndex);
                        parent.removePointer(pointerIndex);

                        sibling.leftSibling = leafNode.leftSibling;
                        if (sibling.leftSibling == null) {
                            leafHead = sibling;
                        }

                        if (parent.hasLowerThenMinimum()) {
                            fixDeficiency(parent);
                        }
                    }else{

                        sortEntries(leafNode.structures);
                    }

                } else if (this.root == null && this.leafHead.numberOfEntry == 0) {

                    this.leafHead = null;
                } else {

                    sortEntries(leafNode.structures);
                }

                return true;
            }
        }
    }

    public boolean updateNode(String id, String description) {

        // if tree is empty
        if (isEmpty()) {
            return false;
        }

        LeafNodes leafNode;
        if (root == null) leafNode = leafHead;
        else leafNode = getPossibleLeafNode(id);
        Structure[] structures = leafNode.structures;
        int index = binarySearch(structures, leafNode.numberOfEntry, id);

        if (index < 0) {
            return false;
        } else {
            structures[index].description = description;
            return true;
        }
    }

    private int binarySearch(Structure[] structures, int numPairs, String t) {
        Comparator<Structure> c = Comparator.comparing(o -> o.partId);
        return Arrays.binarySearch(structures, 0, numPairs, new Structure(t, ""), c);
    }

    private LeafNodes getPossibleLeafNode(String partId) {

        // start from the root
        String[] keys = this.root.productIDs;
        int i;

        for (i = 0; i < this.root.numberOfEntry - 1; i++) {
            if (partId.compareTo(keys[i]) < 0) {
                break;
            }
        }

        Nodes child = this.root.indexPointers[i];
        if (child instanceof LeafNodes) {
            return (LeafNodes) child;
        } else {
            return getPossibleLeafNode((BPlusIndex) child, partId);
        }
    }

    private LeafNodes getPossibleLeafNode(BPlusIndex node, String partId) {
        // start from the root
        String[] keys = node.productIDs;
        int i;

        for (i = 0; i < node.numberOfEntry - 1; i++) {
            if (partId.compareTo(keys[i]) < 0) {
                break;
            }
        }

        Nodes childNodes = node.indexPointers[i];
        if (childNodes instanceof LeafNodes) {
            return (LeafNodes) childNodes;
        } else {
            return getPossibleLeafNode((BPlusIndex) node.indexPointers[i], partId);
        }
    }

    private int getIndexOfPointer(Nodes[] pointers, LeafNodes node) {
        int i;
        i = 0;
        while (i < pointers.length) {
            if (pointers[i] == node) {
                return i;
            }
            i++;
        }
        return i;
    }

    private int getMidPoint() {
        return (int) Math.ceil((BPlusIndex.MAX_ENTRY_KEY + 1) / 2.0) - 1;
    }

    private void fixDeficiency(BPlusIndex in) {

        BPlusIndex sibling;
        BPlusIndex parent = in.parent;

        // start from root node
        if (this.root == in) {
            for (int i = 0; i < in.indexPointers.length; i++) {
                if (in.indexPointers[i] != null) {
                    if (in.indexPointers[i] instanceof BPlusIndex) {
                        this.root = (BPlusIndex) in.indexPointers[i];
                        this.root.parent = null;
                    } else if (in.indexPointers[i] instanceof LeafNodes) {
                        this.root = null;
                    }
                }
            }
        } else if (in.leftSibling != null && in.leftSibling.canLend()) {
            totalFusions++;
            if(in.parent != null){
                parentFusions++;
            }
        } else if (in.rightSibling != null && in.rightSibling.canLend()) {
            sibling = in.rightSibling;

            String siblingFirstId = sibling.productIDs[0];
            Nodes pointer = sibling.indexPointers[0];

            in.productIDs[in.numberOfEntry - 1] = parent.productIDs[0];
            in.indexPointers[in.numberOfEntry] = pointer;

            parent.productIDs[0] = siblingFirstId;

            sibling.removePointer(0);
            Arrays.sort(sibling.productIDs);
            sibling.removePointer(0);
            shiftDown(in.indexPointers);

        } else if (in.rightSibling != null && in.rightSibling.canMerge()) {
            sibling = in.rightSibling;

            sibling.productIDs[sibling.numberOfEntry - 1] = parent.productIDs[parent.numberOfEntry - 2];
            Arrays.sort(sibling.productIDs, 0, sibling.numberOfEntry);
            parent.productIDs[parent.numberOfEntry - 2] = null;

            for (int i = 0; i < in.indexPointers.length; i++) {
                if (in.indexPointers[i] != null) {
                    sibling.prependPointer(in.indexPointers[i]);
                    in.indexPointers[i].parent = sibling;
                    in.removePointer(i);
                }
            }

            parent.removePointer(in);

            sibling.leftSibling = in.leftSibling;

            totalFusions++;
            if (in.parent != null){
                parentFusions++;
            }
        }

        if (parent != null && parent.hasLowerThenMinimum()) {
            fixDeficiency(parent);
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

    private void shiftDown(Nodes[] pointers) {
        Nodes[] newPointers = new Nodes[pointers.length];
        for (int i = 1; i < pointers.length; i++) {
            newPointers[i - 1] = pointers[i];
        }
    }

    private void sortEntries(Structure[] dictionary) {
        Arrays.sort(dictionary, (node1, node2) -> {
            if (node1 == null && node2 == null) {
                return 0;
            }
            if (node1 == null) {
                return 1;
            }
            if (node2 == null) {
                return -1;
            }
            return node1.compareTo(node2);
        });
    }

    private Nodes[] splitChildPointers(BPlusIndex in, int split) {
        Nodes[] pointers = in.indexPointers;
        Nodes[] halfPointers = new Nodes[pointers.length];

        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            in.removePointer(i);
        }

        return halfPointers;
    }

    private Structure[] splitEntries(LeafNodes leafNode, int split) {
        Structure[] dictionary = leafNode.structures;
        Structure[] half = new Structure[dictionary.length];

        for (int i = split; i < dictionary.length; i++) {
            half[i - split] = dictionary[i];
            leafNode.deleteProduct(i);
        }

        return half;
    }

    private void splitIndexNode(BPlusIndex indexNode) {
        BPlusIndex parent = indexNode.parent;

        int mid = getMidPoint();
        String newParentKey = indexNode.productIDs[mid];
        String[] halfKeys = splitIds(indexNode.productIDs, mid);
        Nodes[] halfPointers = splitChildPointers(indexNode, mid);

        indexNode.numberOfEntry = searchEmpty(indexNode.indexPointers);

        // creating new sibling internal node and add half of ids and pointers...
        BPlusIndex sibling = new BPlusIndex(halfKeys, halfPointers);
        for (Nodes pointer : halfPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        sibling.rightSibling = indexNode.rightSibling;
        if (sibling.rightSibling != null) {
            sibling.rightSibling.leftSibling = sibling;
        }
        indexNode.rightSibling = sibling;
        sibling.leftSibling = indexNode;

        if (parent == null) {
            // creating new root node and add mid ids and pointers
            String[] keys = new String[BPlusIndex.MAX_ENTRY_KEY];
            keys[0] = newParentKey;
            BPlusIndex newRoot = new BPlusIndex(keys);
            newRoot.addIndexPointer(indexNode);
            newRoot.addIndexPointer(sibling);
            this.root = newRoot;

            indexNode.parent = newRoot;
            sibling.parent = newRoot;
            totalSplits++;
            treeDepth++;
        } else {

            // add key to parent
            parent.productIDs[parent.numberOfEntry - 1] = newParentKey;
            Arrays.sort(parent.productIDs, 0, parent.numberOfEntry);

            // update pointer to new sibling
            int pointerIndex = parent.getIndexOfPointer(indexNode) + 1;
            parent.insertPointer(sibling, pointerIndex);
            sibling.parent = parent;

            totalSplits++;

            if(parent.parent != null){
                parentSplits++;
            }
        }
    }

    private String[] splitIds(String[] ids, int split) {
        String[] half = new String[BPlusIndex.MAX_ENTRY_KEY];
        ids[split] = null;

        for (int i = split + 1; i < ids.length; i++) {
            half[i - split - 1] = ids[i];
            ids[i] = null;
        }

        return half;
    }

    public void printStaistic(){
        System.out.println("Total TotalSplits: " + totalSplits);
        System.out.println("Total ParentSplits: " + parentSplits);
        System.out.println("Total TotalFusions: " + totalFusions);
        System.out.println("Total ParentFusions: " + parentFusions);
        System.out.println(("Total treeDepth: " + treeDepth));
    }
}
