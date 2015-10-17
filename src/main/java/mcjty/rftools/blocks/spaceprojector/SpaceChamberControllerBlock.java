package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpaceChamberControllerBlock extends GenericRFToolsBlock {

    public SpaceChamberControllerBlock() {
        super(Material.iron, SpaceChamberControllerTileEntity.class, true);
        setBlockName("spaceChamberControllerBlock");
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
            list.add(EnumChatFormatting.WHITE + "This block is one of the eight corners of an");
            list.add(EnumChatFormatting.WHITE + "area of space you want to copy/move elsewhere");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof SpaceChamberControllerTileEntity) {
            SpaceChamberControllerTileEntity spaceChamberControllerTileEntity = (SpaceChamberControllerTileEntity) te;
            int channel = spaceChamberControllerTileEntity.getChannel();
            currenttip.add(EnumChatFormatting.GREEN + "Channel: " + channel);
            if (channel != -1) {
                int size = spaceChamberControllerTileEntity.getChamberSize();
                if (size == -1) {
                    currenttip.add(EnumChatFormatting.YELLOW + "Chamber not formed!");
                } else {
                    currenttip.add(EnumChatFormatting.GREEN + "Area: " + size + " blocks");
                }
            }
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return -1;
    }


    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) {
            world.playSound(x, y, z, "note.pling", 1.0f, 1.0f, false);
        } else {
            SpaceChamberControllerTileEntity chamberControllerTileEntity = (SpaceChamberControllerTileEntity) world.getTileEntity(x, y, z);
            chamberControllerTileEntity.createChamber(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(x, y, z);
            if (te.getChannel() == -1) {
                int id = chamberRepository.newChannel();
                te.setChannel(id);
                chamberRepository.save(world);
            }
            onNeighborBlockChange(world, x, y, z, this);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote) {
            SpaceChamberRepository chamberRepository = SpaceChamberRepository.getChannels(world);
            SpaceChamberControllerTileEntity te = (SpaceChamberControllerTileEntity) world.getTileEntity(x, y, z);
            if (te.getChannel() != -1) {
                chamberRepository.deleteChannel(te.getChannel());
                chamberRepository.save(world);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }


    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":machineSpaceChamberController");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return iconSide;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return iconSide;
    }

}
