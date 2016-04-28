package mcjty.rftools.api.general;

/**
 * If you implement this interface on your inventories then
 * some parts of RFTools (like the storage control module) will be able
 * to efficiently detect if your inventory has changed and if not they
 * can avoid doing some work.
 */
public interface IInventoryTracker {

    /**
     * Get a version number. Whenever your inventory changes in any way you must increase
     * the number that this returns
     */
    int getVersion();
}
