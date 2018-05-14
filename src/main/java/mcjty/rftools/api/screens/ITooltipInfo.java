package mcjty.rftools.api.screens;

import net.minecraft.world.World;

import java.util.List;

/**
 * Implement this interface on your server side screen module
 * (typically the class that also implements IScreenModule) to
 * have support for tooltips (for things like WAILA, TOP, ...)
 */
public interface ITooltipInfo {
    List<String> getInfo(World world, int x, int y);
}
