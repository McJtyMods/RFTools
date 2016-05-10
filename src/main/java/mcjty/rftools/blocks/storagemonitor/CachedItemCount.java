package mcjty.rftools.blocks.storagemonitor;

public class CachedItemCount {
    private final int version;
    private final int count;

    public CachedItemCount(int version, int count) {
        this.count = count;
        this.version = version;
    }

    public int getCount() {
        return count;
    }

    public int getVersion() {
        return version;
    }
}
