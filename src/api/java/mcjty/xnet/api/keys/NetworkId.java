package mcjty.xnet.api.keys;

/**
 * This is a global ID representing a network. It is given by network providers.
 * In a chunk network ID's are coupled with blob ID's.
 */
public class NetworkId {

    private final int id;

    public NetworkId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkId blobId = (NetworkId) o;

        if (id != blobId.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
