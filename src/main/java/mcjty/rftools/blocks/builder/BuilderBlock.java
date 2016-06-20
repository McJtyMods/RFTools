package mcjty.rftools.blocks.builder;

import mcjty.lib.api.IModuleSupport;
import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.ModuleSupport;
import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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
    public boolean isHorizRotation() {
        return true;
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
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This block is linked to a space chamber and");
            list.add(TextFormatting.WHITE + "can move/copy/swap the blocks from the space chamber");
            list.add(TextFormatting.WHITE + "to here. Insert a chamber card to make a link.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(TextFormatting.YELLOW + "increased building speed.");
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
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        TileEntity te = world.getTileEntity(data.getPos());
        if (te instanceof BuilderTileEntity) {
            int scan = BuilderTileEntity.getCurrentLevel();
            probeInfo.text(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof BuilderTileEntity) {
            if (System.currentTimeMillis() - lastTime > 250) {
                lastTime = System.currentTimeMillis();
                BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
                builderTileEntity.requestCurrentLevel();
            }
            int scan = BuilderTileEntity.getCurrentLevel();
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
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiBuilder.class;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_BUILDER;
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
            Achievements.trigger((EntityPlayer) placer, Achievements.theBuilder);
        }
    }
}
