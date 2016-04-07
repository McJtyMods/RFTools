package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.network.PacketGetDestinationInfo;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.network.ReturnDestinationInfoHelper;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MatterTransmitterBlock extends GenericRFToolsBlock implements Infusable {

    public MatterTransmitterBlock() {
        super(Material.iron, MatterTransmitterTileEntity.class, EmptyContainer.class, "matter_transmitter", false);
        setCreativeTab(RFTools.tabRfTools);
        setDefaultState(this.blockState.getBaseState());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiMatterTransmitter.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(MatterTransmitterTileEntity.class, new BeamRenderer());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            list.add(TextFormatting.GREEN + "Name: " + name);

            boolean dialed = false;
            BlockPos c = BlockPosTools.readFromNBT(tagCompound, "dest");
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
                list.add(TextFormatting.YELLOW + "[DIALED to " + destname + "]");
            }

            boolean once = tagCompound.getBoolean("once");
            if (once) {
                list.add(TextFormatting.YELLOW + "[ONCE]");
            }
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "If you place this block near a Dialing Device then");
            list.add(TextFormatting.WHITE + "you can dial it to a Matter Receiver. Make sure to give");
            list.add(TextFormatting.WHITE + "it sufficient power!");
            list.add(TextFormatting.WHITE + "Use a Destination Analyzer adjacent to this block");
            list.add(TextFormatting.WHITE + "to check destination status (red is bad, green ok,");
            list.add(TextFormatting.WHITE + "yellow is unknown).");
            list.add(TextFormatting.WHITE + "Use a  Matter Booster adjacent to this block");
            list.add(TextFormatting.WHITE + "to be able to teleport to unpowered receivers.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(TextFormatting.YELLOW + "increased teleportation speed.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
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
            currenttip.add(TextFormatting.GREEN + "Name: " + matterTransmitterTileEntity.getName());
            if (matterTransmitterTileEntity.isDialed()) {
                if (System.currentTimeMillis() - lastTime > 500) {
                    lastTime = System.currentTimeMillis();
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetDestinationInfo(matterTransmitterTileEntity.getTeleportId()));
                }

                String name = "?";
                if (ReturnDestinationInfoHelper.id != null && ReturnDestinationInfoHelper.id == matterTransmitterTileEntity.getTeleportId()) {
                    name = ReturnDestinationInfoHelper.name;
                }
                currenttip.add(TextFormatting.YELLOW + "[DIALED to " + name + "]");
            }
            if (matterTransmitterTileEntity.isOnce()) {
                currenttip.add(TextFormatting.YELLOW + "[ONCE]");
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

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

}
