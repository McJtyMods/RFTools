package mcjty.rftools.blocks.teleporter;

import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.Logging;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class SimpleDialerBlock extends LogicSlabBlock {

    public SimpleDialerBlock() {
        super("simple_dialer", new BlockBuilder()
            .tileEntitySupplier(SimpleDialerTileEntity::new));
//        super(RFTools.instance, Material.IRON, SimpleDialerTileEntity.class, EmptyContainer::new, SimpleDialerItemBlock::new, "simple_dialer", false);
    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            if (tagCompound.contains("transX")) {
                int transX = tagCompound.getInt("transX");
                int transY = tagCompound.getInt("transY");
                int transZ = tagCompound.getInt("transZ");
                int dim = tagCompound.getInt("transDim");
                list.add(new StringTextComponent(TextFormatting.GREEN + "Transmitter at: " + transX + "," + transY + "," + transZ + " (dim " + dim + ")"));
            }
            if (tagCompound.contains("receiver")) {
                int receiver = tagCompound.getInt("receiver");
                list.add(new StringTextComponent(TextFormatting.GREEN + "Receiver: " + receiver));
            }
            if (tagCompound.getBoolean("once")) {
                list.add(new StringTextComponent(TextFormatting.GREEN + "Dial Once mode enabled"));
            }
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "When this block gets a redstone signal it"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "dials or interrupts a transmitter."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof SimpleDialerTileEntity) {
//            SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity) te;
//            GlobalCoordinate trans = simpleDialerTileEntity.getTransmitter();
//            if (trans != null) {
//                probeInfo.text(TextFormatting.GREEN + "Transmitter at: " + BlockPosTools.toString(trans.getCoordinate()) + " (dim " + trans.getDimension() + ")");
//            }
//            Integer receiver = simpleDialerTileEntity.getReceiver();
//            if (receiver != null) {
//                probeInfo.text(TextFormatting.GREEN + "Receiver: " + receiver);
//            }
//            if (simpleDialerTileEntity.isOnceMode()) {
//                probeInfo.text(TextFormatting.GREEN + "Dial Once mode enabled");
//            }
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currenttip, accessor, config);
//        CompoundNBT tagCompound = accessor.getNBTData();
//        if (tagCompound != null) {
//            if (tagCompound.contains("transX")) {
//                int transX = tagCompound.getInt("transX");
//                int transY = tagCompound.getInt("transY");
//                int transZ = tagCompound.getInt("transZ");
//                int dim = tagCompound.getInt("transDim");
//                currenttip.add(TextFormatting.GREEN + "Transmitter at: " + transX + "," + transY + "," + transZ + " (dim " + dim + ")");
//            }
//            if (tagCompound.contains("receiver")) {
//                int receiver = tagCompound.getInt("receiver");
//                currenttip.add(TextFormatting.GREEN + "Receiver: " + receiver);
//            }
//            if (tagCompound.getBoolean("once")) {
//                currenttip.add(TextFormatting.GREEN + "Dial Once mode enabled");
//            }
//        }
//        return currenttip;
//    }


    @Override
    protected boolean wrenchUse(World world, BlockPos pos, Direction side, PlayerEntity player) {
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_) {
        super.neighborChanged(state, world, pos, blockIn, fromPos, p_220069_6_);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SimpleDialerTileEntity) {
            SimpleDialerTileEntity simpleDialerTileEntity = (SimpleDialerTileEntity) te;
            simpleDialerTileEntity.update();
        }
    }
}
