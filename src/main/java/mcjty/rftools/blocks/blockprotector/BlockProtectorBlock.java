package mcjty.rftools.blocks.blockprotector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.container.EmptyContainer;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class BlockProtectorBlock extends GenericContainerBlock implements Infusable {

    public BlockProtectorBlock() {
        super(Material.iron, BlockProtectorTileEntity.class);
        setBlockName("blockProtectorBlock");
        setHorizRotation(true);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_BLOCK_PROTECTOR;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineBlockProtector";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int id = tagCompound.getInteger("protectorId");
            list.add(EnumChatFormatting.GREEN + "Id: " + id);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Use the smart wrench with this block to select");
            list.add(EnumChatFormatting.WHITE + "other blocks to protect them against explosions");
            list.add(EnumChatFormatting.WHITE + "and other breackage.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power consumption.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof BlockProtectorTileEntity) {
            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
            int id = blockProtectorTileEntity.getId();
            currenttip.add(EnumChatFormatting.GREEN + "Id: " + id);
            currenttip.add(EnumChatFormatting.GREEN + "Blocks protected: " + blockProtectorTileEntity.getProtectedBlocks().size());
        }
        return currenttip;
    }


    @Override
    protected boolean wrenchSneakSelect(World world, int x, int y, int z, EntityPlayer player) {
        if (!world.isRemote) {
            GlobalCoordinate currentBlock = SmartWrenchItem.getCurrentBlock(player.getHeldItem());
            if (currentBlock == null) {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(), new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId));
                RFTools.message(player, EnumChatFormatting.YELLOW + "Selected block");
            } else {
                SmartWrenchItem.setCurrentBlock(player.getHeldItem(), null);
                RFTools.message(player, EnumChatFormatting.YELLOW + "Cleared selected block");
            }
        }
        return true;
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float sx, float sy, float sz, int meta) {
        int rc = super.onBlockPlaced(world, x, y, z, side, sx, sy, sz, meta);
        if (world.isRemote) {
            return rc;
        }
        BlockProtectors protectors = BlockProtectors.getProtectors(world);

        GlobalCoordinate gc = new GlobalCoordinate(new Coordinate(x, y, z), world.provider.dimensionId);

        protectors.getNewId(gc);
        protectors.save(world);

        return rc;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) tileEntity;
        return new GuiBlockProtector(blockProtectorTileEntity, new BlockProtectorContainer(entityPlayer));
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new BlockProtectorContainer(entityPlayer);
    }


    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstoneWithTE(world, x, y, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        // This is called AFTER onBlockPlaced below. Here we need to fix the destination settings.
        if (!world.isRemote) {
            BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) world.getTileEntity(x, y, z);
            blockProtectorTileEntity.getOrCalculateID();
            blockProtectorTileEntity.updateDestination();
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (world.isRemote) {
            return;
        }
        BlockProtectors protectors = BlockProtectors.getProtectors(world);
        protectors.removeDestination(new Coordinate(x, y, z), world.provider.dimensionId);
        protectors.save(world);
    }


}
