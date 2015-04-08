package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpaceProjectorBlock extends GenericContainerBlock {

    private IIcon iconTop;

    public SpaceProjectorBlock() {
        super(Material.iron, SpaceProjectorTileEntity.class);
        setBlockName("spaceProjectorBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            list.add(EnumChatFormatting.GREEN + "Channel: " + channel);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This block is linked to a space chamber and");
            list.add(EnumChatFormatting.WHITE + "can project it on top of this block. Right");
            list.add(EnumChatFormatting.WHITE + "click on a space chamber controller to link");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        NBTTagCompound tagCompound = accessor.getNBTData();
        if (tagCompound != null) {
            int channel = tagCompound.getInteger("channel");
            currenttip.add(EnumChatFormatting.GREEN + "Channel: " + channel);
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        if (!world.isRemote) {
            SpaceProjectorTileEntity spaceProjectorTileEntity = (SpaceProjectorTileEntity) world.getTileEntity(x, y, z);
            RFTools.message((EntityPlayer) entityLivingBase, "Start projecting...");
            spaceProjectorTileEntity.project();
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSpaceProjector");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + "machineBottom");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }


}
