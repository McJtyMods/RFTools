package mcjty.rftools.items.manual;

import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class RFToolsManualItem extends GenericRFToolsItem {

    public RFToolsManualItem() {
        super("rftools_manual");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    protected ActionResult<ItemStack> clOnItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_MANUAL_MAIN, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

}
