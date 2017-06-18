package mcjty.rftools.blocks.monitor;

import mcjty.lib.compat.MyGameReg;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MonitorSetup {
    public static RFMonitorBlock monitorBlock;
    public static LiquidMonitorBlock liquidMonitorBlock;

    public static void init() {
        monitorBlock = new RFMonitorBlock();
        liquidMonitorBlock = new LiquidMonitorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        monitorBlock.initModel();
        liquidMonitorBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;
        MyGameReg.addRecipe(new ItemStack(monitorBlock), " T ", "rMr", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'r', Items.REDSTONE);
        MyGameReg.addRecipe(new ItemStack(liquidMonitorBlock), " T ", "bMb", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'b', Items.BUCKET);
    }
}
