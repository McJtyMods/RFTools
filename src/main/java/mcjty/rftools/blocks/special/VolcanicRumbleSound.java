package mcjty.rftools.blocks.special;

import mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class VolcanicRumbleSound extends MovingSound {
    private final EntityPlayer player;
    private final World world;
    private final int x;
    private final int y;
    private final int z;

    public VolcanicRumbleSound(EntityPlayer player, World world, int x, int y, int z) {
        super(new ResourceLocation(RFTools.MODID + ":deep_rumble"));
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;

        this.field_147666_i = AttenuationType.LINEAR;
        this.repeat = true;
        this.field_147665_h = 0;
    }

    @Override
    public void update() {
        Block block = world.getBlock(x, y, z);
        if (block != SpecialSetup.volcanicCoreBlock) {
            donePlaying = true;
            return;
        }
        volume = 1.0f;
    }
}