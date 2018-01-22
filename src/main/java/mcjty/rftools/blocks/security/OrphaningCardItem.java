package mcjty.rftools.blocks.security;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class OrphaningCardItem extends GenericRFToolsItem {

    public OrphaningCardItem() {
        super("orphaning_card");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Sneak right-click on an RFTools machine to clear");
            list.add(TextFormatting.WHITE + "the owner. You can only do this on blocks you own");
            list.add(TextFormatting.WHITE + "(unless you are admin)");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof GenericTileEntity) {
                // Generalize with a security API interface @todo
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                if (genericTileEntity.getOwnerUUID() == null) {
                    Logging.message(player, TextFormatting.RED + "This block has no owner!");
                } else {
                    if (isPrivileged(player, world)) {
                        genericTileEntity.clearOwner();
                        Logging.message(player, "Cleared owner!");
                    } else if (genericTileEntity.getOwnerUUID().equals(player.getPersistentID())) {
                        genericTileEntity.clearOwner();
                        Logging.message(player, "Cleared owner!");
                    } else {
                        Logging.message(player, TextFormatting.RED + "You cannot clear ownership of a block you don't own!");
                    }
                }
            } else {
                Logging.message(player, TextFormatting.RED + "Onwership is not supported on this block!");
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.SUCCESS;
    }

    public static boolean isPrivileged(EntityPlayer player, World world) {
//        return false;
        return player.capabilities.isCreativeMode || world.getMinecraftServer().getPlayerList().canSendCommands(player.getGameProfile());
    }
}