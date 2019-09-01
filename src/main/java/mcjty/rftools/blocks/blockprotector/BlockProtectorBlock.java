package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.McJtyLib;
import mcjty.lib.api.Infusable;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import mcjty.rftoolsbase.items.SmartWrenchItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class BlockProtectorBlock extends GenericRFToolsBlock implements Infusable /*, IRedstoneConnectable*/ {

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public BlockProtectorBlock() {
        super("block_protector", new BlockBuilder()
            .tileEntitySupplier(BlockProtectorTileEntity::new));
    }

//    @Override
//    public BiFunction<BlockProtectorTileEntity, BlockProtectorContainer, GenericGuiContainer<? super BlockProtectorTileEntity>> getGuiFactory() {
//        return GuiBlockProtector::new;
//    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            int id = tagCompound.getInt("protectorId");
            list.add(new StringTextComponent(TextFormatting.GREEN + "Id: " + id));
        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "Use the smart wrench with this block to select"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "other blocks to protect them against explosions"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "and other breackage."));
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
//        if (te instanceof BlockProtectorTileEntity) {
//            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
//            int id = blockProtectorTileEntity.getId();
//            probeInfo.text(TextFormatting.GREEN + "Id: " + id);
//            probeInfo.text(TextFormatting.GREEN + "Blocks protected: " + blockProtectorTileEntity.getProtectedBlocks().size());
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currenttip, accessor, config);
//        TileEntity te = accessor.getTileEntity();
//        if (te instanceof BlockProtectorTileEntity) {
//            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
//            int id = blockProtectorTileEntity.getId();
//            currenttip.add(TextFormatting.GREEN + "Id: " + id);
//            currenttip.add(TextFormatting.GREEN + "Blocks protected: " + blockProtectorTileEntity.getProtectedBlocks().size());
//        }
//        return currenttip;
//    }

    @Override
    protected boolean wrenchSneakSelect(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            GlobalCoordinate currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem(Hand.MAIN_HAND));
            if (currentBlock == null) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), new GlobalCoordinate(pos, world.getDimension().getType().getId()));
                Logging.message(player, TextFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), null);
                Logging.message(player, TextFormatting.YELLOW + "Cleared selected block");
            }
        }
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState rc = super.getStateForPlacement(context);
        World world = context.getWorld();
        if (world.isRemote) {
            return rc;
        }
        BlockProtectors protectors = BlockProtectors.getProtectors(world);

        BlockPos pos = context.getPos();
        GlobalCoordinate gc = new GlobalCoordinate(pos, world.getDimension().getType().getId());

        protectors.getNewId(gc);
        protectors.save();

        return rc;
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
//        return true;
//    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        if (!world.isRemote) {
            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) world.getTileEntity(pos);
            blockProtectorTileEntity.getOrCalculateID();
            blockProtectorTileEntity.updateDestination();
        }
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        super.onReplaced(state, world, pos, newstate, isMoving);
        if (world.isRemote) {
            return;
        }
        BlockProtectors protectors = BlockProtectors.getProtectors(world);
        protectors.removeDestination(pos, world.getDimension().getType().getId());
        protectors.save();
    }

    // @todo 1.14, proper state handling
//    @Override
//    public BlockState getActualState(BlockState state, IBlockReader world, BlockPos pos) {
//        TileEntity te = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
//        boolean working = false;
//        if (te instanceof BlockProtectorTileEntity) {
//            working = ((BlockProtectorTileEntity)te).isActive();
//        }
//        return state.withProperty(WORKING, working);
//    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return super.canRenderInLayer(state, layer) || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(WORKING);
    }
}
