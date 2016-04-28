package mcjty.rftools.blocks.elevator;

import net.minecraft.block.Block;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElevatorLoopSound extends MovingSound {
    private final World world;
    private final BlockPos pos;

    private float scaleDown = 1.0f;

    public ElevatorLoopSound(World world, int x, int y, int z) {
        super(ElevatorSounds.loopSound, SoundCategory.BLOCKS);
        this.world = world;
        this.pos = new BlockPos(x, y, z);

        this.xPosF = x;
        this.yPosF = y;
        this.zPosF = z;

        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    public void move(float x, float y, float z) {
        xPosF = x;
        yPosF = y;
        zPosF = z;
    }

    @Override
    public void update() {
        Block block = world.getBlockState(pos).getBlock();
        if (block != ElevatorSetup.elevatorBlock) {
            donePlaying = true;
            return;
        }
        volume = ElevatorConfiguration.baseElevatorVolume * scaleDown;
        if (scaleDown > ElevatorConfiguration.loopVolumeFactor) {
            scaleDown -= 0.01f;
        }
    }
}