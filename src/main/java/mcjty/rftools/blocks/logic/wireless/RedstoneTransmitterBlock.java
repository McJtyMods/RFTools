package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericItemBlock;
import mcjty.rftools.RFTools;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class RedstoneTransmitterBlock extends RedstoneChannelBlock<RedstoneTransmitterTileEntity, EmptyContainer> {

    public RedstoneTransmitterBlock() {
        super(Material.IRON, "redstone_transmitter_block", RedstoneTransmitterTileEntity.class, EmptyContainer.class, GenericItemBlock.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiRedstoneTransmitter> getGuiClass() {
        return GuiRedstoneTransmitter.class;
    }

    @Override
    public boolean hasRedstoneOutput() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This logic block accepts redstone signals and");
            list.add(TextFormatting.WHITE + "sends them out wirelessly to linked receivers");
            list.add(TextFormatting.WHITE + "Place down to create a channel or else right");
            list.add(TextFormatting.WHITE + "click on receiver/transmitter to use that channel");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof RedstoneTransmitterTileEntity) {
            probeInfo.text(TextFormatting.GREEN + "Analog mode: " + ((RedstoneTransmitterTileEntity)te).getAnalog());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof RedstoneTransmitterTileEntity) {
            currenttip.add(TextFormatting.GREEN + "Analog mode: " + ((RedstoneTransmitterTileEntity)te).getAnalog());
        }
        return currenttip;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        RedstoneTransmitterTileEntity te = (RedstoneTransmitterTileEntity) worldIn.getTileEntity(pos);
        te.update();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            RedstoneTransmitterTileEntity te = (RedstoneTransmitterTileEntity) world.getTileEntity(pos);
            if (te.getChannel() == -1) {
                RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                int id = redstoneChannels.newChannel();
                te.setChannel(id);
                redstoneChannels.save(world);
            }
            // @todo double check
            te.update();
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_REDSTONE_TRANSMITTER;
    }
}
