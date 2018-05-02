package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.container.LogicSlabBlock;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.screenmodules.ButtonModuleItem;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class RedstoneChannelBlock<T extends RedstoneChannelTileEntity, C extends Container> extends LogicSlabBlock<T, C> {
    public RedstoneChannelBlock(Material material, String name, Class<? extends T> tileEntityClass, Class<? extends C> containerClass, Class<? extends ItemBlock> itemBlockClass) {
        super(RFTools.instance, material, tileEntityClass, containerClass, itemBlockClass, name, false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            list.add(TextFormatting.GREEN + "Channel: " + channel);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof RedstoneChannelTileEntity) {
            probeInfo.text(TextFormatting.GREEN + "Channel: " + ((RedstoneChannelTileEntity)te).getChannel(false));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof RedstoneChannelTileEntity) {
            currenttip.add(TextFormatting.GREEN + "Channel: " + ((RedstoneChannelTileEntity)te).getChannel(false));
        }
        return currenttip;
    }

    private boolean isRedstoneChannelItem(Item item) {
        return (item instanceof ItemBlock && ((ItemBlock)item).getBlock() instanceof RedstoneChannelBlock) || item instanceof ButtonModuleItem;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if(isRedstoneChannelItem(stack.getItem())) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof RedstoneChannelTileEntity) {
                if(!world.isRemote) {
                    RedstoneChannelTileEntity rcte = (RedstoneChannelTileEntity)te;
                    NBTTagCompound tagCompound = stack.getTagCompound();
                    if (tagCompound == null) {
                        tagCompound = new NBTTagCompound();
                        stack.setTagCompound(tagCompound);
                    }
                    int channel;
                    if(!player.isSneaking()) {
                        channel = rcte.getChannel(true);
                        tagCompound.setInteger("channel", channel);
                    } else {
                        if (tagCompound.hasKey("channel")) {
                            channel = tagCompound.getInteger("channel");
                        } else {
                            channel = -1;
                        }
                        if(channel == -1) {
                            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                            channel = redstoneChannels.newChannel();
                            redstoneChannels.save(world);
                            tagCompound.setInteger("channel", channel);
                        }
                        rcte.setChannel(channel);
                    }
                    Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
                }
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
