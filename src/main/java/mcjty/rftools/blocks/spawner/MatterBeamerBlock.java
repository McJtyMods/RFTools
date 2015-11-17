package mcjty.rftools.blocks.spawner;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.api.redstone.IRedstoneConnectable;
import mcjty.lib.api.Infusable;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Optional.InterfaceList({
        @Optional.Interface(iface = "crazypants.enderio.api.redstone.IRedstoneConnectable", modid = "EnderIO")})
public class MatterBeamerBlock extends GenericRFToolsBlock implements Infusable, IRedstoneConnectable {

    private IIcon iconSideOn;

    public MatterBeamerBlock() {
        super(Material.iron, MatterBeamerTileEntity.class, true);
        setBlockName("matterBeamerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
//        NBTTagCompound tagCompound = itemStack.getTagCompound();
//        if (tagCompound != null) {
//            String name = tagCompound.getString("tpName");
//            int id = tagCompound.getInteger("destinationId");
//            list.add(EnumChatFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
//        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This block converts matter into a beam");
            list.add(EnumChatFormatting.WHITE + "of energy. It can then send that beam to");
            list.add(EnumChatFormatting.WHITE + "a connected spawner. Connect by using a wrench.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power usage");
            list.add(EnumChatFormatting.YELLOW + "and increased speed.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof MatterBeamerTileEntity) {
            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) te;
            Coordinate coordinate = matterBeamerTileEntity.getDestination();
            if (coordinate == null) {
                currenttip.add(EnumChatFormatting.RED + "Not connected to a spawner!");
            } else {
                currenttip.add(EnumChatFormatting.GREEN + "Connected!");
            }
        }
        return currenttip;
    }


    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_BEAMER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        MatterBeamerTileEntity beamerTileEntity = (MatterBeamerTileEntity) tileEntity;
        MatterBeamerContainer beamerContainer = new MatterBeamerContainer(entityPlayer, beamerTileEntity);
        return new GuiMatterBeamer(beamerTileEntity, beamerContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new MatterBeamerContainer(entityPlayer, (MatterBeamerTileEntity) tileEntity);
    }

    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) {
            MatterBeamerTileEntity matterBeamerTileEntity = (MatterBeamerTileEntity) world.getTileEntity(x, y, z);
            world.playSound(x, y, z, "note.pling", 1.0f, 1.0f, false);
            matterBeamerTileEntity.useWrench(player);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public boolean shouldRedstoneConduitConnect(World world, int x, int y, int z, ForgeDirection from) {
        return true;
    }

    //    @Override
//    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
//        return true;
//    }
//
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineBeamerOff");
        iconSideOn = iconRegister.registerIcon(RFTools.MODID + ":" + "machineBeamer");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        if ((meta & 1) > 0) {
            return iconSideOn;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return iconSide;
    }

}
