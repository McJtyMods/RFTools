package mcjty.rftools.blocks.spawner;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SpawnerSetup {
    public static SpawnerBlock spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    public static void setupBlocks() {
        spawnerBlock = new SpawnerBlock();
        matterBeamerBlock = new MatterBeamerBlock();
    }

    public static void setupCrafting() {
        GameRegistry.addRecipe(new ItemStack(spawnerBlock), "rzr", "eMl", "rbr", 'M', ModBlocks.machineFrame, 'z', Items.rotten_flesh, 'e', Items.ender_pearl,
                'l', Items.blaze_rod, 'b', Items.bone, 'r', Items.redstone);
        GameRegistry.addRecipe(new ItemStack(matterBeamerBlock), "RGR", "GMG", "RGR", 'M', ModBlocks.machineFrame, 'R', Blocks.redstone_block, 'G', Blocks.glowstone);
    }
}
