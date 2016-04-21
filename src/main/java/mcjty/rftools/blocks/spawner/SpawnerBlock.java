package mcjty.rftools.blocks.spawner;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.varia.RFToolsTools;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class SpawnerBlock extends GenericRFToolsBlock implements Infusable {

    public SpawnerBlock() {
        super(Material.iron, SpawnerTileEntity.class, SpawnerContainer.class, "spawner", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiSpawner.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
//        NBTTagCompound tagCompound = itemStack.getTagCompound();
//        if (tagCompound != null) {
//            String name = tagCompound.getString("tpName");
//            int id = tagCompound.getInteger("destinationId");
//            list.add(EnumChatFormatting.GREEN + "Name: " + name + (id == -1 ? "" : (", Id: " + id)));
//        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This block can spawn creatures. It needs a syringe");
            list.add(TextFormatting.WHITE + "of the appropriate type, RF power and also it");
            list.add(TextFormatting.WHITE + "needs beams of energized matter.");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power usage.");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (heldItem != null && (heldItem.getItem() == ModItems.syringeItem)) {
            if (RFToolsTools.installModule(player, heldItem, hand, pos, SpawnerContainer.SLOT_SYRINGE, SpawnerContainer.SLOT_SYRINGE)) {
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }


    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof SpawnerTileEntity) {
            SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) te;
            float[] matter = spawnerTileEntity.getMatter();
            currenttip.add(TextFormatting.GREEN + "Key Matter: " + matter[0]);
            currenttip.add(TextFormatting.GREEN + "Bulk Matter: " + matter[1]);
            currenttip.add(TextFormatting.GREEN + "Living Matter: " + matter[2]);
        }
        return currenttip;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SPAWNER;
    }

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (world.isRemote) {
            SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) world.getTileEntity(pos);
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvent.soundEventRegistry.getObject(new ResourceLocation("block.note.pling")), SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            spawnerTileEntity.useWrench(player);
        }
        return true;
    }
}
