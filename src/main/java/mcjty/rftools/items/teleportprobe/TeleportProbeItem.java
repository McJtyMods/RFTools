package mcjty.rftools.items.teleportprobe;

import mcjty.rftools.RFTools;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TeleportProbeItem extends Item {

    public TeleportProbeItem() {
        setUnlocalizedName("teleport_probe");
        setRegistryName("teleport_probe");
        setCreativeTab(RFTools.tabRfTools);
        setMaxStackSize(1);
        GameRegistry.register(this);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_TELEPORTPROBE, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

//    @Override
//    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
//        if (world.isRemote) {
//            player.openGui(RFTools.instance, RFTools.GUI_TELEPORTPROBE, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
//            return true;
//        }
//        return true;
//    }
}