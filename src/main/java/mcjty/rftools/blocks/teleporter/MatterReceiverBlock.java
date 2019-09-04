package mcjty.rftools.blocks.teleporter;

import mcjty.lib.McJtyLib;
import mcjty.lib.api.Infusable;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MatterReceiverBlock extends GenericRFToolsBlock implements Infusable {

    public MatterReceiverBlock() {
        super("matter_receiver", new BlockBuilder()
            .tileEntitySupplier(MatterReceiverTileEntity::new));
//        setDefaultState(this.blockState.getBaseState());
    }

//    @SideOnly(Side.CLIENT)
//    @Override
//    public BiFunction<MatterReceiverTileEntity, EmptyContainer, GenericGuiContainer<? super MatterReceiverTileEntity>> getGuiFactory() {
//        return GuiMatterReceiver::new;
//    }


    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, world, list, advanced);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            String name = tagCompound.getString("tpName");
            int id = tagCompound.getInt("destinationId");
            list.add(new StringTextComponent(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id))));
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "If you place this block anywhere in the world then"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "you can dial to it using a Dialing Device. Before"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "teleporting to this block make sure to give it power!"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof MatterReceiverTileEntity) {
//            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
//            String name = matterReceiverTileEntity.getModuleName();
//            int id = matterReceiverTileEntity.getId();
//            if (name == null || name.isEmpty()) {
//                probeInfo.text(TextFormatting.GREEN + (id == -1 ? "" : ("Id: " + id)));
//            } else {
//                probeInfo.text(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
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
//        if (te instanceof MatterReceiverTileEntity) {
//            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
//            String name = matterReceiverTileEntity.getModuleName();
//            int id = matterReceiverTileEntity.getId();
//            if (name == null || name.isEmpty()) {
//                currenttip.add(TextFormatting.GREEN + (id == -1 ? "" : ("Id: " + id)));
//            } else {
//                currenttip.add(TextFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
//            }
//        }
//        return currenttip;
//    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        World world = context.getWorld();
        if (world.isRemote) {
            return state;
        }
        TeleportDestinations destinations = TeleportDestinations.get();

        BlockPos pos = context.getPos();
        GlobalCoordinate gc = new GlobalCoordinate(pos, world.getDimension().getType().getId());

        destinations.getNewId(gc);
        destinations.addDestination(gc);
        destinations.save();

        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // We don't want what BaseBlock does.
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        // @todo 1.14 check
//        restoreBlockFromNBT(world, pos, stack);
        if (!world.isRemote) {
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) world.getTileEntity(pos);
            matterReceiverTileEntity.getOrCalculateID();
            matterReceiverTileEntity.updateDestination();
        }
        setOwner(world, pos, placer);
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        super.onReplaced(state, world, pos, newstate, isMoving);
        if (world.isRemote) {
            return;
        }
        TeleportDestinations destinations = TeleportDestinations.get();
        destinations.removeDestination(pos, world.getDimension().getType().getId());
        destinations.save();
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }
}
