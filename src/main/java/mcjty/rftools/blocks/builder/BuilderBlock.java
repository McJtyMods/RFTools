package mcjty.rftools.blocks.builder;

import crazypants.enderio.api.redstone.IRedstoneConnectable;
import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class BuilderBlock extends GenericRFToolsBlock<BuilderTileEntity, BuilderContainer> implements Infusable, IRedstoneConnectable {

    public BuilderBlock() {
        super(Material.iron, BuilderTileEntity.class, BuilderContainer.class, "builder", true);
    }

    @Override
    public boolean isHorizRotation() {
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        checkRedstoneWithTE(world, pos);
    }

    @Override
    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, EnumFacing from) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This block is linked to a space chamber and");
            list.add(EnumChatFormatting.WHITE + "can move/copy/swap the blocks from the space chamber");
            list.add(EnumChatFormatting.WHITE + "to here. Insert a chamber card to make a link.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(EnumChatFormatting.YELLOW + "increased building speed.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof BuilderTileEntity) {
            BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
            if (System.currentTimeMillis() - lastTime > 250) {
                lastTime = System.currentTimeMillis();
                builderTileEntity.requestCurrentLevel();
            }
            int scan = builderTileEntity.getCurrentLevel();
            currenttip.add(EnumChatFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
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
}
