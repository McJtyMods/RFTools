package mcjty.xnet.api.keys;

/**
 * This is a global ID representing a consumer.
 */
public class ConsumerId {

    private final int id;

    public ConsumerId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsumerId blobId = (ConsumerId) o;

        if (id != blobId.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
