package mcjty.rftools.blocks.security;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.List;

public class OrphaningCardItem extends Item {

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
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
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
                    } else if (genericTileEntity.getOwnerUUID().equals(player.getUniqueID())) {
                        genericTileEntity.clearOwner();
                        Logging.message(player, "Cleared owner!");
                    } else {
                        Logging.message(player, TextFormatting.RED + "You cannot clear ownership of a block you don't own!");
                    }
                }
            } else {
                Logging.message(player, TextFormatting.RED + "Onwership is not supported on this block!");
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }

    public static boolean isPrivileged(PlayerEntity player, World world) {
//        return false;
        return player.abilities.isCreativeMode || ServerLifecycleHooks.getCurrentServer().getPlayerList().canSendCommands(player.getGameProfile());
    }
}