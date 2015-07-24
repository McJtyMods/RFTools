package mcjty.rftools.blocks.endergen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.container.EmptyContainer;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class EndergenicBlock extends GenericRFToolsBlock implements Infusable {

    private IIcon icon;
    private IIcon iconUp;

    public EndergenicBlock() {
        super(Material.iron, EndergenicTileEntity.class, true);
        setBlockName("endergenicBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERGENIC;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Generate power out of ender pearls. You need at");
            list.add(EnumChatFormatting.WHITE + "least two generators for this to work and the setup");
            list.add(EnumChatFormatting.WHITE + "is relatively complicated. Timing is crucial.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: increased power generation and");
            list.add(EnumChatFormatting.YELLOW + "reduced powerloss for holding pearls.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) tileEntity;
        EmptyContainer endergenicContainer = new EmptyContainer(entityPlayer);
        return new GuiEndergenic(endergenicTileEntity, endergenicContainer);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "machineEndergenic");
        iconUp = iconRegister.registerIcon(RFTools.MODID + ":" + "machineEndergenicUp");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        checkForMonitor4Sides(world, x, y, z);
        if (entityLivingBase instanceof EntityPlayer) {
            Achievements.trigger((EntityPlayer) entityLivingBase, Achievements.hardPower);
        }
    }

    private void checkForMonitor4Sides(World world, int x, int y, int z) {
        if (!world.isRemote) {
            checkForMonitor(world, x+1, y, z);
            checkForMonitor(world, x-1, y, z);
            checkForMonitor(world, x, y, z+1);
            checkForMonitor(world, x, y, z - 1);
        }
    }

    private void checkForMonitor(World world, int x, int y, int z) {
        if (EndergenicSetup.enderMonitorBlock.equals(world.getBlock(x, y, z))) {
            EndergenicSetup.enderMonitorBlock.registerWithEndergenic(world, x, y, z);
        }
    }

    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) {
            EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) world.getTileEntity(x, y, z);
            world.playSound(x, y, z, "note.pling", 1.0f, 1.0f, false);
            endergenicTileEntity.useWrench(player);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return getIcon(side, 0);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconUp;
        } else {
            return icon;
        }
    }

}
