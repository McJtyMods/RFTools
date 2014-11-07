package com.mcjty.rftools.items.devdelight;

import com.mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DevelopersDelightItem extends Item {

    public DevelopersDelightItem() {
        setMaxStackSize(1);
    }

//    @Override
//    public boolean itemInteractionForEntity(ItemStack p_111207_1_, EntityPlayer p_111207_2_, EntityLivingBase p_111207_3_) {
//        return super.itemInteractionForEntity(p_111207_1_, p_111207_2_, p_111207_3_);
//    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (world.isRemote) {
            GuiDevelopersDelight.setSelected(x, y, z);
            player.openGui(RFTools.instance, RFTools.GUI_DEVELOPERS_DELIGHT, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return true;
        }
        return true;
    }

}
