package mcjty.rftools.blocks.shield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import mcjty.varia.Logging;
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
            list.add(EnumChatFormatting.WHITE + "This machine forms a shield out of adjacent");
            list.add(EnumChatFormatting.WHITE + "template blocks. It can filter based on type of");
            list.add(EnumChatFormatting.WHITE + "mob and do various things (damage, solid, ...)");
            list.add(EnumChatFormatting.WHITE + "Use the Smart Wrench to add sections to the shield");
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
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.isRemote) {
            composeDecomposeShield(world, x, y, z, true);
        }
    }

    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        composeDecomposeShield(world, x, y, z, false);
        return true;
    }

    @Override
    protected boolean wrenchSneakSelect(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.isRemote) {
            GlobalCoordinate currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem());
            if (currentBlock == null) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(), new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId));
                Logging.message(player, EnumChatFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(), null);
                Logging.message(player, EnumChatFormatting.YELLOW + "Cleared selected block");
            }
        }
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
