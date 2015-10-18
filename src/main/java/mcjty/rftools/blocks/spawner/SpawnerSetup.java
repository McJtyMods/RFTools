package mcjty.rftools.blocks.spawner;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class SpawnerSetup {
    public static SpawnerBlock spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    public static void setupBlocks() {
        spawnerBlock = new SpawnerBlock();
        GameRegistry.registerBlock(spawnerBlock, GenericItemBlock.class, "spawnerBlock");
        GameRegistry.registerTileEntity(SpawnerTileEntity.class, "SpawnerTileEntity");
        matterBeamerBlock = new MatterBeamerBlock();
        GameRegistry.registerBlock(matterBeamerBlock, GenericItemBlock.class, "matterBeamerBlock");
        GameRegistry.registerTileEntity(MatterBeamerTileEntity.class, "MatterBeamerTileEntity");
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(spawnerBlock), "rzr", "eMl", "rbr", 'M', ModBlocks.machineFrame, 'z', Items.rotten_flesh, 'e', Items.ender_pearl,
                'l', Items.blaze_rod, 'b', Items.bone, 'r', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(matterBeamerBlock), "RGR", "GMG", "RGR", 'M', ModBlocks.machineFrame, 'R', Blocks.redstone_block, 'G', Blocks.glowstone);
    }
}
