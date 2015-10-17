package mcjty.rftools.blocks.monitor;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MonitorSetup {
    public static RFMonitorBlock monitorBlock;
    public static LiquidMonitorBlock liquidMonitorBlock;

    public static void setupBlocks() {
        monitorBlock = new RFMonitorBlock();
        GameRegistry.registerBlock(monitorBlock, GenericItemBlock.class, "rfMonitorBlock");
        GameRegistry.registerTileEntity(RFMonitorBlockTileEntity.class, "RFMonitorTileEntity");

        liquidMonitorBlock = new LiquidMonitorBlock();
        GameRegistry.registerBlock(liquidMonitorBlock, GenericItemBlock.class, "liquidMonitorBlock");
        GameRegistry.registerTileEntity(LiquidMonitorBlockTileEntity.class, "LiquidMonitorBlockTileEntity");
    }

    public static void setupCrafting() {
        Object redstoneTorch = Item.itemRegistry.getObject("redstone_torch");
        GameRegistry.addRecipe(new ItemStack(monitorBlock), " T ", "rMr", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'r', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(liquidMonitorBlock), " T ", "bMb", " T ", 'M', ModBlocks.machineFrame, 'T', redstoneTorch, 'b', Items.bucket);
    }
}
