package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MatterReceiverBlock extends GenericRFToolsBlock<MatterReceiverTileEntity, EmptyContainer> implements Infusable {

    public MatterReceiverBlock() {
        super(Material.IRON, MatterReceiverTileEntity.class, EmptyContainer::new, "matter_receiver", false);
        setDefaultState(this.blockState.getBaseState());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiMatterReceiver> getGuiClass() {
        return GuiMatterReceiver.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            int id = tagCompound.getInteger("destinationId");
            list.add(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "If you place this block anywhere in the world then");
            list.add(TextFormatting.WHITE + "you can dial to it using a Dialing Device. Before");
            list.add(TextFormatting.WHITE + "teleporting to this block make sure to give it power!");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof MatterReceiverTileEntity) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
            String name = matterReceiverTileEntity.getName();
            int id = matterReceiverTileEntity.getId();
            if (name == null || name.isEmpty()) {
                probeInfo.text(TextFormatting.GREEN + (id == -1 ? "" : ("Id: " + id)));
            } else {
                probeInfo.text(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof MatterReceiverTileEntity) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
            String name = matterReceiverTileEntity.getName();
            int id = matterReceiverTileEntity.getId();
            if (name == null || name.isEmpty()) {
                currenttip.add(TextFormatting.GREEN + (id == -1 ? "" : ("Id: " + id)));
            } else {
                currenttip.add(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
            }
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_RECEIVER;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        if (world.isRemote) {
            return state;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);

        GlobalCoordinate gc = new GlobalCoordinate(pos, world.provider.getDimension());

        destinations.getNewId(gc);
        destinations.addDestination(gc);
        destinations.save();

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        // We don't want what GenericBlock does.
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        restoreBlockFromNBT(world, pos, stack);
        if (!world.isRemote) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) world.getTileEntity(pos);
            matterReceiverTileEntity.getOrCalculateID();
            matterReceiverTileEntity.updateDestination();
        }
        setOwner(world, pos, placer);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (world.isRemote) {
            return;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.removeDestination(pos, world.provider.getDimension());
        destinations.save();
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }
}
