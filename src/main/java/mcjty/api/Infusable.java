package mcjty.api;

/**
 * Implement this interface on your block if you want your block to be
 * infusable with the RFTools Machine Infuser. As soon as this interface
 * is implemented then inserting your block in the Machine Infuser will
 * set increase the 'infused' integer NBT tag on the ItemBlock for
 * your machine. This is a number that goes from 0 (not infused)
 * to 256 (maximum infused).
 */
public interface Infusable {
}
