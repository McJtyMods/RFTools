package mcjty.rftools.blocks.spaceprojector;

import mcjty.lib.container.EmptyContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpaceChamberControllerBlock extends GenericRFToolsBlock<SpaceChamberControllerTileEntity, EmptyContainer> {

    public SpaceChamberControllerBlock() {
        super(Material.iron, SpaceChamberControllerTileEntity.class, EmptyContainer.class, "space_chamber_controller", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            list.add(EnumChatFormatting.GREEN + "Channel: " + channel);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This block is one of the eight corners of an");
            list.add(EnumChatFormatting.WHITE + "area of space you want to copy/move elsewhere");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
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
            currenttip.add(EnumChatFormatting.GREEN + "Channel: " + channel);
            if (channel != -1) {
                int size = spaceChamberControllerTileEntity.getChamberSize();
                if (size == -1) {
                    currenttip.add(EnumChatFormatting.YELLOW + "Chamber not formed!");
                } else {
                    currenttip.add(EnumChatFormatting.GREEN + "Area: " + size + " blocks");
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
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), "note.pling", 1.0f, 1.0f, false);
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
            onNeighborBlockChange(world, pos, state, this);
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
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
