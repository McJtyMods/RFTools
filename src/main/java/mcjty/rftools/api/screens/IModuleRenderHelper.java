package mcjty.rftools.api.screens;

import mcjty.rftools.api.screens.data.IModuleDataContents;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;

/**
 * Some convenience methods to help render screen modules
 */
public interface IModuleRenderHelper {

    @Deprecated
    void renderLevel(FontRenderer fontRenderer, int xoffset, int currenty, IModuleDataContents screenData, String label, boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff,
                     int poscolor, int negcolor,
                     int gradient1, int gradient2, FormatStyle formatStyle);

    /**
     * Create a text render helper that you can use to render text for your module. This will
     * automatically support truetype if so enabled in rftools
     */
    ITextRenderHelper createTextRenderHelper();

    /**
     * Create a level render helper that you can use to render progress bars/energy bars/...
     */
    ILevelRenderHelper createLevelRenderHelper();

    String format(String in, FormatStyle style);


    /**
     * Simple text render. This version does not support size or alignment. Use ITextRenderHelper
     * if you want that.
     * @param x
     * @param y
     * @param color
     * @param renderInfo
     * @param text
     */
    void renderText(int x, int y, int color, @Nonnull ModuleRenderInfo renderInfo, String text);

    /**
     * Simple text render. This version does not support size or alignment. Use ITextRenderHelper
     * if you want that. This version does support truncating the text to the given width
     * @param x
     * @param y
     * @param color
     * @param renderInfo
     * @param text
     * @param maxwidth Use 512 for full screen width
     */
    void renderTextTrimmed(int x, int y, int color, @Nonnull ModuleRenderInfo renderInfo, String text, int maxwidth);
}
