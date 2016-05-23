package mcjty.rftools.blocks.teleporter;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.logic.LogicSlabBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SimpleDialerBlock extends LogicSlabBlock<SimpleDialerTileEntity, EmptyContainer> {

    public SimpleDialerBlock() {
        super(Material.IRON, "simple_dialer", SimpleDialerTileEntity.class, EmptyContainer.class, SimpleDialerItemBlock.class);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            if (tagCompound.hasKey("transX")) {
                int transX = tagCompound.getInteger("transX");
                int transY = tagCompound.getInteger("transY");
                int transZ = tagCompound.getInteger("transZ");
                int dim = tagCompound.getInteger("transDim");
                list.add(TextFormatting.GREEN + "Transmitter at: " + transX + "," + transY + "," + transZ + " (dim " + dim + ")");
            }
            if (tagCompound.hasKey("receiver")) {
                int receiver = tagCompound.getInteger("receiver");
                list.add(TextFormatting.GREEN + "Receiver: " + receiver);
            }
            if (tagCompound.getBoolean("once")) {
                list.add(TextFormatting.GREEN + "Dial Once mode enabled");
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "When this block gets a redstone signal it");
            list.add(TextFormatting.WHITE + "dials or interrupts a transmitter.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            if (tagCompound.hasKey("transX")) {
                int transX = tagCompound.getInteger("transX");
                int transY = tagCompound.getInteger("transY");
                int transZ = tagCompound.getInteger("transZ");
                int dim = tagCompound.getInteger("transDim");
                currenttip.add(TextFormatting.GREEN + "Transmitter at: " + transX + "," + transY + "," + transZ + " (dim " + dim + ")");
            }
            if (tagCompound.hasKey("receiver")) {
                int receiver = tagCompound.getInteger("receiver");
                currenttip.add(TextFormatting.GREEN + "Receiver: " + receiver);
            }
            if (tagCompound.getBoolean("once")) {
                currenttip.add(TextFormatting.GREEN + "Dial Once mode enabled");
            }
        }
        return currenttip;
    }


    @Override
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (!world.isRemote) {
            SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity) world.getTileEntity(pos);
            boolean onceMode = !simpleDialerTileEntity.isOnceMode();
            simpleDialerTileEntity.setOnceMode(onceMode);
            if (onceMode) {
                Logging.message(player, "Enabled 'dial once' mode");
            } else {
                Logging.message(player, "Disabled 'dial once' mode");
            }
        }
        return true;
    }


    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
        super.neighborChanged(state, world, pos, blockIn);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SimpleDialerTileEntity) {
            SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity) te;
            simpleDialerTileEntity.update();
        }
    }

    @Override
    public int getGuiID() {
        return -1;
    }
}
