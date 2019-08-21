package mcjty.rftools.blocks.builder;

import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class SpaceChamberControllerBlock extends GenericRFToolsBlock {

    public SpaceChamberControllerBlock() {
        super("space_chamber_controller", new BlockBuilder()
                .tileEntitySupplier(SpaceChamberControllerTileEntity::new));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            int channel = tagCompound.getInt("channel");
            list.add(new StringTextComponent(TextFormatting.GREEN + "Channel: " + channel));
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This block is one of the eight corners of an"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "area of space you want to copy/move elsewhere"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof SpaceChamberControllerTileEntity) {
//            SpaceChamberControllerTileEntity spaceChamberControllerTileEntity = (SpaceChamberControllerTileEntity) te;
//            int channel = spaceChamberControllerTileEntity.getChannel();
//            probeInfo.text(TextFormatting.GREEN + "Channel: " + channel);
//            if (channel != -1) {
//                int size = spaceChamberControllerTileEntity.getChamberSize();
//                if (size == -1) {
//                    probeInfo.text(TextFormatting.YELLOW + "Chamber not formed!");
//                } else {
//                    probeInfo.text(TextFormatting.GREEN + "Area: " + size + " blocks");
//                }
//            }
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currenttip, accessor, config);
//        TileEntity te = accessor.getTileEntity();
//        if (te instanceof SpaceChamberControllerTileEntity) {
//            SpaceChamberControllerTileEntity spaceChamberControllerTileEntity = (SpaceChamberControllerTileEntity) te;
//            int channel = spaceChamberControllerTileEntity.getChannel();
//            currenttip.add(TextFormatting.GREEN + "Channel: " + channel);
//            if (channel != -1) {
//                int size = spaceChamberControllerTileEntity.getChamberSize();
//                if (size == -1) {
//                    currenttip.add(TextFormatting.YELLOW + "Chamber not formed!");
//                } else {
//                    currenttip.add(TextFormatting.GREEN + "Area: " + size + " blocks");
//                }
//            }
//        }
//        return currenttip;
//    }

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, Direction side, PlayerEntity player) {
        if (world.isRemote) {
            SoundEvent pling = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.note.pling"));
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), pling, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        } else {
            SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            chamberControllerTileEntity.createChamber(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entityLivingBase, itemStack);
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            if (te.getChannel() == -1) {
                int id = chamberRepository.newChannel();
                te.setChannel(id);
                chamberRepository.save();
            }
            // @todo
//            onNeighborBlockChange(world, pos, state, this);
        }
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(pos);
            if (te.getChannel() != -1) {
                chamberRepository.deleteChannel(te.getChannel());
                chamberRepository.save();
            }
        }
        super.onReplaced(state, world, pos, newstate, isMoving);
    }

// @todo 1.14
//    @Override
//    public boolean isBlockNormalCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.TRANSLUCENT;
//    }
}
