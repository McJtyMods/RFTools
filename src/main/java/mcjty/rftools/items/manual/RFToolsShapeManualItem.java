package mcjty.rftools.items.manual;

import mcjty.rftools.RFTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class RFToolsShapeManualItem extends Item {

    public RFToolsShapeManualItem() {
        super(new Item.Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("rftools_shape_manual");
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(new StringTextComponent("RFTools shape manual"));
        tooltip.add(new StringTextComponent("The Builder, Shield, Shapecards,"));
        tooltip.add(new StringTextComponent("The Scanner, Projector, Composor,"));
        tooltip.add(new StringTextComponent("and Locator"));
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_MANUAL_SHAPE, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

}
