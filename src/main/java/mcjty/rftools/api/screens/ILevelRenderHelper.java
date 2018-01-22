package mcjty.rftools.api.screens;

import mcjty.rftools.api.screens.data.IModuleDataContents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This level render helper knows how to render progress/energy/level bars
 */
public interface ILevelRenderHelper {

    void render(int x, int y, @Nullable IModuleDataContents data, @Nonnull ModuleRenderInfo renderInfo);

    ILevelRenderHelper label(String label);

    ILevelRenderHelper settings(boolean hidebar, boolean hidetext, boolean showpct, boolean showdiff);

    ILevelRenderHelper color(int poscolor, int negcolor);

    ILevelRenderHelper gradient(int gradient1, int gradient2);

    ILevelRenderHelper format(FormatStyle formatStyle);
}
