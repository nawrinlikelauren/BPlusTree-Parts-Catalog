public class Structure implements Comparable<Structure>{
    String partId;
    String description;

    public Structure(String partId, String description) {
        this.partId = partId;
        this.description = description;
    }

    public String getId() {
        return partId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Structure o) {
        return partId.compareTo(o.partId);
    }

    @Override
    public String toString() {
        return "[" + partId +
                " : " + description +
                ']';
    }
}
