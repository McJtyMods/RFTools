package mcjty.rftools.api.screens;

import mcjty.rftools.api.screens.data.IModuleData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * This interface represents the client-side module. This will be called with
 * the data from the server.
 *
 * @param <T>
 */
public interface IClientScreenModule<T extends IModuleData> {
    enum TransformMode {
        NONE,
        TEXT,
        TEXTLARGE,
        ITEM
    }

    TransformMode getTransformMode();

    int getHeight();

    /**
     * Here you actually render your module. Warning! Always check if screenData is actually set! It is possible
     * you get null here which can mean that you don't need server data or the data isn't ready yet. Prepare for that.
     */
    void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, T screenData, float factor);

    /**
     * For interactive modules you can implement this to detect if your module was clickedd
     *
     * @param world
     * @param x
     * @param y
     * @param clicked
     */
    void mouseClick(World world, int x, int y, boolean clicked);

    /**
     * Create the gui for your module.
     *
     * @param guiBuilder
     */
    void createGui(IModuleGuiBuilder guiBuilder);

    /**
     * This is called when your module is being instantiated from a saved world
     * so you can setup your data. The tags that are given to the tagCompound
     * depend on how you set up your GUI in the IClientScreenModule.
     *  @param tagCompound
     * @param dim the dimension for the screen this module is in
     * @param pos the position of the screen
     */
    void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos);

    // Return true if this module needs server data.
    boolean needsServerData();
}
