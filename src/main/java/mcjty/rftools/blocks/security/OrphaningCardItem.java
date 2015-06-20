package mcjty.rftools.blocks.security;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.entity.GenericTileEntity;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class OrphaningCardItem extends Item {

    public OrphaningCardItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Sneak right-click on an RFTools machine to clear");
            list.add(EnumChatFormatting.WHITE + "the owner. You can only do this on blocks you own");
            list.add(EnumChatFormatting.WHITE + "(unless you are admin)");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof GenericTileEntity) {
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                if (genericTileEntity.getOwnerUUID() == null) {
                    RFTools.message(player, EnumChatFormatting.RED + "This block has no owner!");
                } else {
                    if (player.capabilities.isCreativeMode || MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile())) {
                        genericTileEntity.clearOwner();
                        RFTools.message(player, "Cleared owner!");
                    } else if (genericTileEntity.getOwnerUUID().equals(player.getPersistentID())) {
                        genericTileEntity.clearOwner();
                        RFTools.message(player, "Cleared owner!");
                    } else {
                        RFTools.message(player, EnumChatFormatting.RED + "You cannot clear ownership of a block you don't own!");
                    }
                }
            } else {
                RFTools.message(player, EnumChatFormatting.RED + "Onwership is not supported on this block!");
            }
            return true;
        }
        return true;
    }
}