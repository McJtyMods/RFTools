package mcjty.rftools.blocks.booster;

import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoosterSetup {
    public static BoosterBlock boosterBlock;

    public static void init() {
        boosterBlock = new BoosterBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        boosterBlock.initModel();
    }

    public static void initCrafting() {
        GameRegistry.addRecipe(new ItemStack(boosterBlock), "oDo", "GMI", "oEo", 'o', Items.glass_bottle, 'M', ModBlocks.machineFrame,
                'D', Items.diamond, 'E', Items.redstone, 'G', Items.gold_ingot, 'I', Items.iron_ingot);
    }
}
