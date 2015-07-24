package mcjty.rftools.blocks.relay;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.container.EmptyContainer;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.RFTools;
import mcjty.varia.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
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

public class RelayBlock extends GenericRFToolsBlock {

    private IIcon icons[] = new IIcon[6];
    private IIcon iconFrontOff;

    public RelayBlock() {
        super(Material.iron, RelayTileEntity.class, false);
        setBlockName("relayBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_RELAY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        RelayTileEntity relayTileEntity = (RelayTileEntity) tileEntity;
        return new GuiRelay(relayTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineRelay_on";
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        iconFrontOff = iconRegister.registerIcon(RFTools.MODID + ":" + "machineRelay");
        icons[ForgeDirection.DOWN.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineRelayD");
        icons[ForgeDirection.UP.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineRelayU");
        icons[ForgeDirection.NORTH.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineRelayN");
        icons[ForgeDirection.SOUTH.ordinal()] = null;//iconRegister.registerIcon(RFTools.MODID + ":machineRelay_on");
        icons[ForgeDirection.WEST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineRelayW");
        icons[ForgeDirection.EAST.ordinal()] = iconRegister.registerIcon(RFTools.MODID + ":machineRelayE");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This device can (based on a redstone signal) limit");
            list.add(EnumChatFormatting.WHITE + "the amount of RF that can go through this. Using this");
            list.add(EnumChatFormatting.WHITE + "you can throttle down (or even disable) a number of");
            list.add(EnumChatFormatting.WHITE + "machines in case power is low.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection direction = BlockTools.reorient(ForgeDirection.values()[side], meta);
        if (direction == ForgeDirection.SOUTH) {
            return getIconInd(blockAccess, x, y, z, meta);
        }
        return icons[direction.ordinal()];
    }

    @Override
    public IIcon getIconInd(IBlockAccess blockAccess, int x, int y, int z, int meta) {
        boolean rs = BlockTools.getRedstoneSignal(meta);
        if (rs) {
            return iconInd;
        } else {
            return iconFrontOff;
        }
    }
}
