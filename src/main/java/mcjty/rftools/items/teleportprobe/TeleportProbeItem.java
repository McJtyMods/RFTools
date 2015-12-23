package mcjty.rftools.items.teleportprobe;

import mcjty.rftools.RFTools;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TeleportProbeItem extends Item {

    public TeleportProbeItem() {
        setUnlocalizedName("teleport_probe");
        setCreativeTab(RFTools.tabRfTools);
        setMaxStackSize(1);
        GameRegistry.registerItem(this, "teleport_probe");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_TELEPORTPROBE, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

//    @Override
//    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
//        if (world.isRemote) {
//            player.openGui(RFTools.instance, RFTools.GUI_TELEPORTPROBE, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
//            return true;
//        }
//        return true;
//    }
}