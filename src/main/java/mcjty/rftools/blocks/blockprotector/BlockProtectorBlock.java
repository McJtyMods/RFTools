package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.api.Infusable;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.rftools.setup.GuiProxy;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.BiFunction;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class BlockProtectorBlock extends GenericRFToolsBlock<BlockProtectorTileEntity, BlockProtectorContainer> implements Infusable /*, IRedstoneConnectable*/ {

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public BlockProtectorBlock() {
        super(Material.IRON, BlockProtectorTileEntity.class, BlockProtectorContainer::new, "block_protector", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<BlockProtectorTileEntity, BlockProtectorContainer, GenericGuiContainer<? super BlockProtectorTileEntity>> getGuiFactory() {
        return GuiBlockProtector::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_BLOCK_PROTECTOR;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            int id = tagCompound.getInteger("protectorId");
            list.add(TextFormatting.GREEN + "Id: " + id);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "Use the smart wrench with this block to select");
            list.add(TextFormatting.WHITE + "other blocks to protect them against explosions");
            list.add(TextFormatting.WHITE + "and other breackage.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof BlockProtectorTileEntity) {
            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
            int id = blockProtectorTileEntity.getId();
            probeInfo.text(TextFormatting.GREEN + "Id: " + id);
            probeInfo.text(TextFormatting.GREEN + "Blocks protected: " + blockProtectorTileEntity.getProtectedBlocks().size());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof BlockProtectorTileEntity) {
            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
            int id = blockProtectorTileEntity.getId();
            currenttip.add(TextFormatting.GREEN + "Id: " + id);
            currenttip.add(TextFormatting.GREEN + "Blocks protected: " + blockProtectorTileEntity.getProtectedBlocks().size());
        }
        return currenttip;
    }

    @Override
    protected boolean wrenchSneakSelect(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            GlobalCoordinate currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem(Hand.MAIN_HAND));
            if (currentBlock == null) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), new GlobalCoordinate(pos, world.provider.getDimension()));
                Logging.message(player, TextFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(Hand.MAIN_HAND), null);
                Logging.message(player, TextFormatting.YELLOW + "Cleared selected block");
            }
        }
        return true;
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        BlockState rc = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        if (world.isRemote) {
            return rc;
        }
        BlockProtectors protectors = BlockProtectors.getProtectors(world);

        GlobalCoordinate gc = new GlobalCoordinate(pos, world.provider.getDimension());

        protectors.getNewId(gc);
        protectors.save();

        return rc;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
//        return true;
//    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        if (!world.isRemote) {
            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) world.getTileEntity(pos);
            blockProtectorTileEntity.getOrCalculateID();
            blockProtectorTileEntity.updateDestination();
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        super.breakBlock(world, pos, state);
        if (world.isRemote) {
            return;
        }
        BlockProtectors protectors = BlockProtectors.getProtectors(world);
        protectors.removeDestination(pos, world.provider.getDimension());
        protectors.save();
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
        boolean working = false;
        if (te instanceof BlockProtectorTileEntity) {
            working = ((BlockProtectorTileEntity)te).isActive();
        }
        return state.withProperty(WORKING, working);
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return super.canRenderInLayer(state, layer) || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, WORKING);
    }
}
