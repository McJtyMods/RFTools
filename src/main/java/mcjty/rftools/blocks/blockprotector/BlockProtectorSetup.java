package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.compat.MyGameReg;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.shield.ShieldSetup;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockProtectorSetup {
    public static BlockProtectorBlock blockProtectorBlock;

    public static void init() {
        blockProtectorBlock = new BlockProtectorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        blockProtectorBlock.initModel();
    }

    public static void initCrafting() {
        if (GeneralConfiguration.enableBlockProtectorRecipe) {
            MyGameReg.addRecipe(new ItemStack(blockProtectorBlock), "oto", "tMt", "oto", 'M', ModBlocks.machineFrame, 'o', Blocks.OBSIDIAN, 't', ShieldSetup.shieldTemplateBlock);
        }
    }
}
