package mcjty.rftools.api.screens;

/**
 * A builder to help create gui's for your screen modules. The 'tagname' parameter is what will
 * be set in the NBT. With 'monitor' it will use the tags 'monitorx', 'monitory', 'monitorz', and 'dim'.
 *
 * You will get an instance of this builder in IClientScreenModule.createGui(). The gui is line based.
 * This means that components will be placed on a single line until you call the 'nl' function here.
 *
 * <example>
 *     public void createGui(IModuleGuiBuilder builder) {
 *         // Create a gui with two lines. First line containing a label and a text input
 *         // field that will be saved with the name 'tag' in the NBT of the module.
 *         // The second line containing a label and a color that is saved with the name
 *         // 'colorTag' in the NBT
 *         builder.label("Label1").text("tag").nl();
 *         builder.label("Label2").color("colorTag", "Set color").nl();
 *     }
 * </example>
 */
public interface IModuleGuiBuilder {

    IModuleGuiBuilder label(String text);

    IModuleGuiBuilder leftLabel(String text);

    IModuleGuiBuilder text(String tagname, String... tooltip);

    IModuleGuiBuilder integer(String tagname, String... tooltip);

    IModuleGuiBuilder toggle(String tagname, String label, String... tooltip);

    IModuleGuiBuilder toggleNegative(String tagname, String label, String... tooltip);

    /**
     * A color selector.
     *
     * @param tagname the tag that will be used to save the format in your NBT. The format
     *                is saved as an integer color value.
     * @param tooltip
     * @return
     */
    IModuleGuiBuilder color(String tagname, String... tooltip);

    /**
     * A combobox component that can be used to specify a format. This allows
     * the user of your module to specify any of the possible FormatStyle values.
     *
     * @param tagname the tag that will be used to save the format in your NBT. The format
     *                is saved as an integer ordinal value for the FormatStyle enum.
     * @return
     */
    IModuleGuiBuilder format(String tagname);

    /**
     * This is a more hardcoded component that can be used to control the
     * mode to display a 'level' amount in. It gives the player a component that
     * he/she can configure to show a bar, show as a difference (RF/tick), optionally
     * hide the text or show as a percentage. It will save this information in the
     * three boolean tags: 'showdiff', 'showpct', and 'hidetext'.
     *
     * @param componentName is the name of the unit we are displaying (example 'RF').
     * @return
     */
    IModuleGuiBuilder mode(String componentName);

    /**
     * This is a read-only component that shows the block at the given
     * position and dimension. It gets this information from the NBT with
     * the tagnamePos and suffix 'x', 'y', or 'z' as well as the dimension.
     * So for example if 'tagnamePos' is equal to 'block' then the following
     * tags are supported:
     * <list>
     *     <li>blockx</li>
     *     <li>blocky</li>
     *     <li>blockz</li>
     *     <li>blockdim (dimension)</li>
     *     <li>blockname (optional name to show in the gui)</li>
     * </list>
     * This is usually used in combination with a module item that overrides onItemUse
     * to be able to set the target of this module to a specific block. The onItemUse
     * implementation must then set these same tags.
     *
     * @param tagname
     * @return
     */
    IModuleGuiBuilder block(String tagname);

    /**
     * Add a gui component for a ghost stack. This allows the users to select ghost
     * stacks.
     *
     * @param tagname the tag that will be used to save the format in your NBT. The format
     *                is saved as an integer ordinal value for the FormatStyle enum.
     * @return
     */
    IModuleGuiBuilder ghostStack(String tagname);

    /**
     * Perform a new line in your GUI.
     * @return
     */
    IModuleGuiBuilder nl();
}
