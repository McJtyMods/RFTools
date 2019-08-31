package mcjty.rftools.blocks.shaper;

import mcjty.rftools.blocks.builder.BuilderSetup;
import net.minecraft.block.Block;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class ProjectorSound extends TickableSound {
    private final World world;
    private final BlockPos pos;

    public ProjectorSound(World world, int x, int y, int z) {
        super(ProjectorSounds.scanSound, SoundCategory.BLOCKS);
        this.world = world;
        this.pos = new BlockPos(x, y, z);

        this.x = x;
        this.y = y;
        this.z = z;

        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    public void stop() {
        donePlaying = true;
    }

    public void move(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void tick() {
        Block block = world.getBlockState(pos).getBlock();
        if (block != BuilderSetup.projectorBlock) {
            donePlaying = true;
            return;
        }
        volume = (float) (double) ScannerConfiguration.baseProjectorVolume.get();
    }
}