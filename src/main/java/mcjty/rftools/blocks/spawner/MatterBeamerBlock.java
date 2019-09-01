package mcjty.rftools.blocks.spawner;

import mcjty.lib.McJtyLib;
import mcjty.lib.api.Infusable;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class MatterBeamerBlock extends GenericRFToolsBlock implements Infusable /*, IRedstoneConnectable*/ {

    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public MatterBeamerBlock() {
        super("matter_beamer", new BlockBuilder()
            .tileEntitySupplier(MatterBeamerTileEntity::new));
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    // @todo 1.14
//    @Override
//    public void initModel() {
//        super.initModel();
//        MatterBeamerRenderer.register();
//    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader player, List<ITextComponent> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
//        CompoundNBT tagCompound = itemStack.getTag();
//        if (tagCompound != null) {
//            String name = tagCompound.getString("tpName");
//            int id = tagCompound.getInt("destinationId");
//            list.add(EnumChatFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
//        }
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This block converts matter into a beam"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "of energy. It can then send that beam to"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "a connected spawner. Connect by using a wrench."));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Infusing bonus: reduced power usage"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "increased speed and less material needed"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof MatterBeamerTileEntity) {
//            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) te;
//            BlockPos coordinate = matterBeamerTileEntity.getDestination();
//            if (coordinate == null) {
//                probeInfo.text(TextFormatting.RED + "Not connected to a spawner!");
//            } else {
//                probeInfo.text(TextFormatting.GREEN + "Connected!");
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
//        if (te instanceof MatterBeamerTileEntity) {
//            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) te;
//            BlockPos coordinate = matterBeamerTileEntity.getDestination();
//            if (coordinate == null) {
//                currenttip.add(TextFormatting.RED + "Not connected to a spawner!");
//            } else {
//                currenttip.add(TextFormatting.GREEN + "Connected!");
//            }
//        }
//        return currenttip;
//    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public BiFunction<MatterBeamerTileEntity, MatterBeamerContainer, GenericGuiContainer<? super MatterBeamerTileEntity>> getGuiFactory() {
//        return GuiMatterBeamer::new;
//    }

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, Direction side, PlayerEntity player) {
        if (world.isRemote) {
            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) world.getTileEntity(pos);
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.note.pling")), SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            matterBeamerTileEntity.useWrench(player);
        }
        return true;
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state, IBlockReader world, BlockPos pos) {
//        TileEntity te = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
//        boolean working = false;
//        if (te instanceof MatterBeamerTileEntity) {
//            working = ((MatterBeamerTileEntity)te).isGlowing();
//        }
//        return state.withProperty(WORKING, working);
//    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(WORKING);
    }
}
