package mcjty.rftools.blocks.builder;

import mcjty.lib.container.EmptyContainer;
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
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpaceChamberControllerBlock extends GenericRFToolsBlock<SpaceChamberControllerTileEntity, EmptyContainer> {

    public SpaceChamberControllerBlock() {
        super(Material.IRON, SpaceChamberControllerTileEntity.class, EmptyContainer.class, "space_chamber_controller", true);
    }

    @Override
    public boolean hasNoRotation() {
        return true;
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

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This block is one of the eight corners of an");
            list.add(TextFormatting.WHITE + "area of space you want to copy/move elsewhere");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof SpaceChamberControllerTileEntity) {
            SpaceChamberControllerTileEntity spaceChamberControllerTileEntity = (SpaceChamberControllerTileEntity) te;
            int channel = spaceChamberControllerTileEntity.getChannel();
            probeInfo.text(TextFormatting.GREEN + "Channel: " + channel);
            if (channel != -1) {
                int size = spaceChamberControllerTileEntity.getChamberSize();
                if (size == -1) {
                    probeInfo.text(TextFormatting.YELLOW + "Chamber not formed!");
                } else {
                    probeInfo.text(TextFormatting.GREEN + "Area: " + size + " blocks");
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof SpaceChamberControllerTileEntity) {
            SpaceChamberControllerTileEntity spaceChamberControllerTileEntity = (SpaceChamberControllerTileEntity) te;
            int channel = spaceChamberControllerTileEntity.getChannel();
            currenttip.add(TextFormatting.GREEN + "Channel: " + channel);
            if (channel != -1) {
                int size = spaceChamberControllerTileEntity.getChamberSize();
                if (size == -1) {
                    currenttip.add(TextFormatting.YELLOW + "Chamber not formed!");
                } else {
                    currenttip.add(TextFormatting.GREEN + "Area: " + size + " blocks");
                }
            }
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return -1;
    }


    @Override
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (world.isRemote) {
            SoundEvent pling = SoundEvent.REGISTRY.getObject(new ResourceLocation("block.note.pling"));
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), pling, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        } else {
            SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            chamberControllerTileEntity.createChamber(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entityLivingBase, itemStack);
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            if (te.getChannel() == -1) {
                int id = chamberRepository.newChannel();
                te.setChannel(id);
                chamberRepository.save(world);
            }
            // @todo
//            onNeighborBlockChange(world, pos, state, this);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            if (te.getChannel() != -1) {
                chamberRepository.deleteChannel(te.getChannel());
                chamberRepository.save(world);
            }
        }
        super.breakBlock(world, pos, state);
    }


    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
}
