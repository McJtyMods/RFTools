package mcjty.rftools.varia;

/**
 * Implement this for an enum with names and descriptions. Used
 * inside gui's for example.
 */
public interface NamedEnum {

    /// Used for displaying on the combo button
    String getName();

    /// Used as the tooltip
    String[] getDescription();

    public static <T extends NamedEnum> T getEnumByName(String name, T[] values) {
        for (T value : values) {
            if (name.equals(value.getName())) {
                return value;
            }
        }
        return null;
    }
}
