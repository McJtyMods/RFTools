package mcjty.rftools.api.screens;

/**
 * This text render helper knows how to render text in vanilla
 * or truetype font on the screen
 */
public interface ITextRenderHelper {

    /**
     * Set up this render helper for a given line of text, a given width and the
     * renderInfo you get from RFTools. This function should be called every time before
     * rendering text. If nothing changed since last time (the helper is not dirty) then
     * this will not do anything.
     * @param line the line of text to render. Will be truncated to available space
     * @param width use a width of 512 for the entire width of the screen
     * @param renderInfo
     */
    void setup(String line, int width, ModuleRenderInfo renderInfo);

    String getText();

    /**
     * Force a recalculation of the text.
     */
    void setDirty();

    boolean isLarge();

    /**
     * Enable large mode (double size font)
     * @param large
     */
    ITextRenderHelper large(boolean large);

    TextAlign getAlign();

    /**
     * Set alignment option
     * @param align
     */
    ITextRenderHelper align(TextAlign align);

    /**
     * Actually render the text. The given coordinates are in 128x128 space with 0,0 being the top-left
     * of the screen
     * @param x
     * @param y
     * @param color
     * @param renderInfo
     */
    void renderText(int x, int y, int color, ModuleRenderInfo renderInfo);
}
