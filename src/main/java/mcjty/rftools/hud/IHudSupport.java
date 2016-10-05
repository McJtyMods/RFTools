package mcjty.rftools.hud;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Implement in a TE
 */
public interface IHudSupport {

    EnumFacing getBlockOrientation();

    boolean isBlockAboveAir();

    List<String> getClientLog();

    long getLastUpdateTime();

    void setLastUpdateTime(long t);

    BlockPos getPos();
}
