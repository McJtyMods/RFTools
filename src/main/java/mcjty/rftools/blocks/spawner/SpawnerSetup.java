package mcjty.rftools.blocks.spawner;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpawnerSetup {
    public static SpawnerBlock spawnerBlock;
    public static MatterBeamerBlock matterBeamerBlock;

    public static void init() {
        spawnerBlock = new SpawnerBlock();
        matterBeamerBlock = new MatterBeamerBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        spawnerBlock.initModel();
        matterBeamerBlock.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(spawnerBlock), "rzr", "eMl", "rbr", 'M', ModBlocks.machineFrame, 'z', Items.ROTTEN_FLESH, 'e', Items.ENDER_PEARL,
                'l', Items.BLAZE_ROD, 'b', Items.BONE, 'r', Items.REDSTONE);
        GameRegistry.addRecipe(new ItemStack(matterBeamerBlock), "RGR", "GMG", "RGR", 'M', ModBlocks.machineFrame, 'R', Blocks.REDSTONE_BLOCK, 'G', Blocks.GLOWSTONE);
    }
}
