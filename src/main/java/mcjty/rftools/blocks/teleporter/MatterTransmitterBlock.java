package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.network.PacketGetDestinationInfo;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.network.ReturnDestinationInfoHelper;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MatterTransmitterBlock extends GenericRFToolsBlock implements Infusable {

    public MatterTransmitterBlock() {
        super(Material.iron, MatterTransmitterTileEntity.class, EmptyContainer.class, GuiMatterTransmitter.class, "matter_transmitter", false);
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            list.add(EnumChatFormatting.GREEN + "Name: " + name);

            boolean dialed = false;
            BlockPos c = Coordinate.readFromNBT(tagCompound, "dest");
            if (c != null && c.getY() >= 0) {
                dialed = true;
            } else if (tagCompound.hasKey("destId")) {
                if (tagCompound.getInteger("destId") != -1) {
                    dialed = true;
                }
            }

            if (dialed) {
                int destId = tagCompound.getInteger("destId");
                if (System.currentTimeMillis() - lastTime > 500) {
                    lastTime = System.currentTimeMillis();
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetDestinationInfo(destId));
                }

                String destname = "?";
                if (ReturnDestinationInfoHelper.id != null && ReturnDestinationInfoHelper.id == destId) {
                    destname = ReturnDestinationInfoHelper.name;
                }
                list.add(EnumChatFormatting.YELLOW + "[DIALED to " + destname + "]");
            }

            boolean once = tagCompound.getBoolean("once");
            if (once) {
                list.add(EnumChatFormatting.YELLOW + "[ONCE]");
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "If you place this block near a Dialing Device then");
            list.add(EnumChatFormatting.WHITE + "you can dial it to a Matter Receiver. Make sure to give");
            list.add(EnumChatFormatting.WHITE + "it sufficient power!");
            list.add(EnumChatFormatting.WHITE + "Use a Destination Analyzer adjacent to this block");
            list.add(EnumChatFormatting.WHITE + "to check destination status (red is bad, green ok,");
            list.add(EnumChatFormatting.WHITE + "yellow is unknown).");
            list.add(EnumChatFormatting.WHITE + "Use a  Matter Booster adjacent to this block");
            list.add(EnumChatFormatting.WHITE + "to be able to teleport to unpowered receivers.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(EnumChatFormatting.YELLOW + "increased teleportation speed.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof MatterTransmitterTileEntity) {
            MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) te;
            currenttip.add(EnumChatFormatting.GREEN + "Name: " + matterTransmitterTileEntity.getName());
            if (matterTransmitterTileEntity.isDialed()) {
                if (System.currentTimeMillis() - lastTime > 500) {
                    lastTime = System.currentTimeMillis();
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetDestinationInfo(matterTransmitterTileEntity.getTeleportId()));
                }

                String name = "?";
                if (ReturnDestinationInfoHelper.id != null && ReturnDestinationInfoHelper.id == matterTransmitterTileEntity.getTeleportId()) {
                    name = ReturnDestinationInfoHelper.name;
                }
                currenttip.add(EnumChatFormatting.YELLOW + "[DIALED to " + name + "]");
            }
            if (matterTransmitterTileEntity.isOnce()) {
                currenttip.add(EnumChatFormatting.YELLOW + "[ONCE]");
            }
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_TRANSMITTER;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        // We don't want what GenericBlock does.
        restoreBlockFromNBT(world, pos, stack);
        setOwner(world, pos, placer);
    }
}
