package mcjty.rftools;

import mcjty.lib.varia.Coordinate;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class holds information on client-side only which are global to the mod.
 */
public class ClientInfo {
    private Coordinate selectedTE = null;
    private Coordinate destinationTE = null;

    private Coordinate hilightedBlock = null;
    private long expireHilight = 0;

    public void hilightBlock(Coordinate c, long expireHilight) {
        hilightedBlock = c;
        this.expireHilight = expireHilight;
    }

    public Coordinate getHilightedBlock() {
        return hilightedBlock;
    }

    public long getExpireHilight() {
        return expireHilight;
    }

    public Coordinate getSelectedTE() {
        return selectedTE;
    }

    public void setSelectedTE(Coordinate selectedTE) {
        this.selectedTE = selectedTE;
    }

    public Coordinate getDestinationTE() {
        return destinationTE;
    }

    public void setDestinationTE(Coordinate destinationTE) {
        this.destinationTE = destinationTE;
    }

    @SideOnly(Side.CLIENT)
    public static World getWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
