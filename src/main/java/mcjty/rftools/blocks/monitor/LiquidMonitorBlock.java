package mcjty.rftools.blocks.monitor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.varia.BlockTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class LiquidMonitorBlock extends GenericRFToolsBlock {
    private IIcon iconFront0;
    private IIcon iconFront1;
    private IIcon iconFront2;
    private IIcon iconFront3;
    private IIcon iconFront4;

    public LiquidMonitorBlock() {
        super(Material.iron, LiquidMonitorBlockTileEntity.class, false);
        setBlockName("liquidMonitorBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineFrontLiquid";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_LIQUID_MONITOR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        LiquidMonitorBlockTileEntity liquidMonitorBlockTileEntity = (LiquidMonitorBlockTileEntity) tileEntity;
        return new GuiLiquidMonitor(liquidMonitorBlockTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        iconFront0 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFrontLiquid_0");
        iconFront1 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFrontLiquid_1");
        iconFront2 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFrontLiquid_2");
        iconFront3 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFrontLiquid_3");
        iconFront4 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineFrontLiquid_4");
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        return BlockTools.getRedstoneSignal(metadata) ? 15 : 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int metadata = blockAccess.getBlockMetadata(x, y, z);
        return BlockTools.getRedstoneSignal(metadata) ? 15 : 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This device monitors the amount of liquid in an adjacent");
            list.add(EnumChatFormatting.WHITE + "machine (select it with the GUI). It can also send");
            list.add(EnumChatFormatting.WHITE + "out a redstone signal if the liquid goes above or below");
            list.add(EnumChatFormatting.WHITE + "some value.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public IIcon getIconInd(IBlockAccess blockAccess, int x, int y, int z, int meta) {
        TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
        LiquidMonitorBlockTileEntity liquidMonitorBlockTileEntity = (LiquidMonitorBlockTileEntity) tileEntity;
        int fluidlevel = liquidMonitorBlockTileEntity.getFluidLevel();
        switch (fluidlevel) {
            case 1: return iconFront0;
            case 2: return iconFront1;
            case 3: return iconFront2;
            case 4: return iconFront3;
            case 5: return iconFront4;
            default: return iconInd;

        }
    }
}
