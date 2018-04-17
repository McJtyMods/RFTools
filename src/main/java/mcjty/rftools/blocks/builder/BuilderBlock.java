package mcjty.rftools.blocks.builder;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.api.Infusable;
import mcjty.lib.varia.ModuleSupport;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

//@Optional.InterfaceList({
//        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class BuilderBlock extends GenericRFToolsBlock<BuilderTileEntity, BuilderContainer> implements Infusable /*, IRedstoneConnectable */ {

    public BuilderBlock() {
        super(Material.IRON, BuilderTileEntity.class, BuilderContainer.class, "builder", true);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.HORIZROTATION;
    }

    @Override
    public boolean needsRedstoneCheck() {
        return true;
    }

//    @Override
//    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
//        return true;
//    }


    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(BuilderTileEntity.class, new BuilderRenderer());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This block can quarry areas, pump liquids,");
            list.add(TextFormatting.WHITE + "move/copy/swap structures, collect items");
            list.add(TextFormatting.WHITE + "and XP, move entities, build structures, ...");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption");
            list.add(TextFormatting.YELLOW + "and increased speed.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    protected IModuleSupport getModuleSupport() {
        return new ModuleSupport(BuilderContainer.SLOT_TAB) {
            @Override
            public boolean isModule(ItemStack itemStack) {
                return (itemStack.getItem() == BuilderSetup.shapeCardItem || itemStack.getItem() == BuilderSetup.spaceChamberCardItem);
            }
        };
    }

    private static long lastTime = 0;

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof BuilderTileEntity) {
            int scan = ((BuilderTileEntity) te).getCurrentLevel();
            probeInfo.text(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof BuilderTileEntity) {
            if (System.currentTimeMillis() - lastTime > 250) {
                lastTime = System.currentTimeMillis();
                BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
                builderTileEntity.requestCurrentLevel();
            }
            int scan = BuilderTileEntity.getCurrentLevelClientSide();
            currenttip.add(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
        }
        return currenttip;

    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        boolean rc = super.rotateBlock(world, pos, axis);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BuilderTileEntity) {
            if (!world.isRemote) {
                BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
                if (builderTileEntity.hasSupportMode()) {
                    builderTileEntity.clearSupportBlocks();
                    builderTileEntity.resetBox();
                }
            }
        }
        return rc;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiBuilder> getGuiClass() {
        return GuiBuilder.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_BUILDER;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState metadata, int fortune) {
        super.getDrops(drops, world, pos, metadata, fortune);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BuilderTileEntity) {
            List<ItemStack> overflowItems = ((BuilderTileEntity)te).getOverflowItems();
            if(overflowItems != null) {
                drops.addAll(overflowItems);
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BuilderTileEntity) {
            if (!world.isRemote) {
                BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
                if (builderTileEntity.hasSupportMode()) {
                    builderTileEntity.clearSupportBlocks();
                }
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof EntityPlayer) {
            // @todo achievements
//            Achievements.trigger((EntityPlayer) placer, Achievements.theBuilder);
        }
    }
}
