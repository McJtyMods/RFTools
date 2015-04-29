package mcjty.rftools.blocks.spawner;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.api.Infusable;
import mcjty.container.GenericContainerBlock;
import mcjty.rftools.RFTools;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
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

public class SpawnerBlock extends GenericContainerBlock implements Infusable {

    public SpawnerBlock() {
        super(Material.iron, SpawnerTileEntity.class);
        setBlockName("spawnerBlock");
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
            list.add(EnumChatFormatting.WHITE + "This block can spawn creatures. It needs a syringe");
            list.add(EnumChatFormatting.WHITE + "of the appropriate type, RF power and also it");
            list.add(EnumChatFormatting.WHITE + "needs beams of energized matter.");
            list.add(EnumChatFormatting.YELLOW + "Infusing bonus: reduced power usage.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof SpawnerTileEntity) {
            SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) te;
            float[] matter = spawnerTileEntity.getMatter();
            currenttip.add(EnumChatFormatting.GREEN + "Key Matter: " + matter[0]);
            currenttip.add(EnumChatFormatting.GREEN + "Bulk Matter: " + matter[1]);
            currenttip.add(EnumChatFormatting.GREEN + "Living Matter: " + matter[2]);
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SPAWNER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) tileEntity;
        SpawnerContainer spawnerContainer = new SpawnerContainer(entityPlayer, spawnerTileEntity);
        return new GuiSpawner(spawnerTileEntity, spawnerContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new SpawnerContainer(entityPlayer, (SpawnerTileEntity) tileEntity);
    }

    @Override
    protected boolean wrenchUse(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) {
            SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) world.getTileEntity(x, y, z);
            world.playSound(x, y, z, "note.pling", 1.0f, 1.0f, false);
            spawnerTileEntity.useWrench(player);
        }
        return true;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineSpawner";
    }

}
