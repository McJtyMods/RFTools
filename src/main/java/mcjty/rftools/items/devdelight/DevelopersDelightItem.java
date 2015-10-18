package mcjty.rftools.items.devdelight;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
            dumpInfo(world, x, y, z);
            GuiDevelopersDelight.setSelected(x, y, z);
            player.openGui(RFTools.instance, RFTools.GUI_DEVELOPERS_DELIGHT, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return true;
        }
        return true;
    }

    private void dumpInfo(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == null || block.getMaterial() == Material.air) {
            return;
        }
        int meta = world.getBlockMetadata(x, y, z);
        String modid = RFToolsTools.getModidForBlock(block);
        Logging.log("Block: " + block.getUnlocalizedName() + ", Meta: " + meta + ", Mod: " + modid);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            NBTTagCompound tag = new NBTTagCompound();
            try {
                tileEntity.writeToNBT(tag);
                StringBuffer buffer = new StringBuffer();
                RFToolsTools.convertNBTtoJson(buffer, tag, 0);
                Logging.log(buffer.toString());
            } catch (Exception e) {
                Logging.log("Catched a crash during dumping of NBT");
            }
        }
    }


}
