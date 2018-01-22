package mcjty.rftools.api.screens;

import mcjty.lib.font.TrueTypeFont;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * Class containing various things useful for rendering your module. You get an instance of this
 * in IClientScreenModule.render()
 */
public class ModuleRenderInfo {

    // A factor representing the size of the screen (1, 2, or 3)
    public final float factor;

    // The position of the screen block
    public final BlockPos pos;

    // If the mouse is pointing at this module then this is the relative x inside the module
    public final int hitx;

    // If the mouse is pointing at this module then this is the relative y inside the module
    public final int hity;

    // Non null if this should be rendered with truetype
    public final TrueTypeFont font;

    public ModuleRenderInfo(float factor, BlockPos pos, int hitx, int hity, @Nullable TrueTypeFont font) {
        this.factor = factor;
        this.pos = pos;
        this.hitx = hitx;
        this.hity = hity;
        this.font = font;
    }
}
