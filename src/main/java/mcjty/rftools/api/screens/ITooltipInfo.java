package mcjty.rftools.api.screens;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Implement this interface on your server side screen module
 * (typically the class that also implements IScreenModule) to
 * have support for tooltips (for things like WAILA, TOP, ...)
 */
public interface ITooltipInfo {
    String[] getInfo(World world, int x, int y, EntityPlayer player);
}
