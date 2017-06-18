package mcjty.rftools.blocks.elevator;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ElevatorSetup {
    public static ElevatorBlock elevatorBlock;

    public static void init() {
        elevatorBlock = new ElevatorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        elevatorBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

    }

}
