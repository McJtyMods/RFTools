package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.container.GenericBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class BuilderBlock extends GenericBlock implements Infusable {

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
