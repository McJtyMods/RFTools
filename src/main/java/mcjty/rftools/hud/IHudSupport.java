package mcjty.rftools.hud;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Implement in a TE
 */
public interface IHudSupport {

    Direction getBlockOrientation();

    boolean isBlockAboveAir();

    List<String> getClientLog();

    long getLastUpdateTime();

    void setLastUpdateTime(long t);

    BlockPos getBlockPos();
}
