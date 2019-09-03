package mcjty.rftools.blocks.elevator;

import net.minecraft.block.Block;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;



public class ElevatorStartupSound extends TickableSound {
    private final World world;
    private final BlockPos pos;

    public ElevatorStartupSound(World world, int x, int y, int z) {
        super(ElevatorSounds.startSound, SoundCategory.BLOCKS);
        this.world = world;
        this.pos = new BlockPos(x, y, z);

        this.x = x;
        this.y = y;
        this.z = z;

        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    public void move(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void tick() {
        Block block = world.getBlockState(pos).getBlock();
        if (block != ElevatorSetup.elevatorBlock) {
            donePlaying = true;
            return;
        }
        volume = (float) (double) ElevatorConfiguration.baseElevatorVolume.get();
    }
}