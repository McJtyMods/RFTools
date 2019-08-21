package mcjty.rftools.items.teleportprobe;

import mcjty.rftools.RFTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;



public class TeleportProbeItem extends Item {

    public TeleportProbeItem() {
        super(new Properties()
                .maxStackSize(1)
                .defaultMaxDamage(1)
                .group(RFTools.setup.getTab()));
        setRegistryName("teleport_probe");
    }

//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_TELEPORTPROBE, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}