package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.api.redstone.IRedstoneConnectable;
import mcjty.lib.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class BuilderBlock extends GenericRFToolsBlock implements Infusable, IRedstoneConnectable {

    public BuilderBlock() {
        super(Material.iron, BuilderTileEntity.class, true);
        setBlockName("builderBlock");
        setCreativeTab(RFTools.tabRfTools);
        setHorizRotation(true);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstoneWithTE(world, x, y, z);
    }

    @Override
    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
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
    protected void rotateBlock(World world, int x, int y, int z) {
        super.rotateBlock(world, x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof BuilderTileEntity) {
            if (!world.isRemote) {
                BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
                if (builderTileEntity.hasSupportMode()) {
                    builderTileEntity.clearSupportBlocks();
                    builderTileEntity.resetBox();
                }
            }
        }
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineBuilder";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_BUILDER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        BuilderTileEntity builderTileEntity = (BuilderTileEntity) tileEntity;
        BuilderContainer builderContainer = new BuilderContainer(entityPlayer, builderTileEntity);
        return new GuiBuilder(builderTileEntity, builderContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new BuilderContainer(entityPlayer, (BuilderTileEntity) tileEntity);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof BuilderTileEntity) {
            if (!world.isRemote) {
                BuilderTileEntity builderTileEntity = (BuilderTileEntity) te;
                if (builderTileEntity.hasSupportMode()) {
                    builderTileEntity.clearSupportBlocks();
                }
            }
        }

        super.breakBlock(world, x, y, z, block, meta);
    }
}
