package mcjty.rftools.blocks.shield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ShieldBlock extends GenericRFToolsBlock implements Infusable {

    public ShieldBlock(String blockName, Class<? extends ShieldTEBase> clazz) {
        super(Material.iron, clazz, true);
        setBlockName(blockName);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SHIELD;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        ShieldTEBase shieldTileEntity = (ShieldTEBase) tileEntity;
        ShieldContainer shieldContainer = new ShieldContainer(entityPlayer, shieldTileEntity);
        return new GuiShield(shieldTileEntity, shieldContainer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This machine can build a shield out of adjacent");
            list.add(EnumChatFormatting.WHITE + "shield template block. You can add filters for");
            list.add(EnumChatFormatting.WHITE + "players, mobs, animals, items to control if they");
            list.add(EnumChatFormatting.WHITE + "should be able to pass or not and if they should");
            list.add(EnumChatFormatting.WHITE + "get damage.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption and");
            list.add(EnumChatFormatting.YELLOW + "increased damage.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new ShieldContainer(entityPlayer, (ShieldTEBase) tileEntity);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        restoreBlockFromNBT(world, x, y, z, itemStack);
        setOwner(world, x, y, z, entityLivingBase);
    }

    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        composeDecomposeShield(world, x, y, z, ctrl);
        return true;
    }

    private void composeDecomposeShield(World world, int x, int y, int z, boolean ctrl) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ShieldTEBase) {
                ((ShieldTEBase)te).composeDecomposeShield(ctrl);
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof ShieldTEBase) {
            if (!world.isRemote) {
                ShieldTEBase shieldTileEntity = (ShieldTEBase) te;
                if (shieldTileEntity.isShieldComposed()) {
                    shieldTileEntity.decomposeShield();
                }
            }
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineShieldProjector");
    }
}
