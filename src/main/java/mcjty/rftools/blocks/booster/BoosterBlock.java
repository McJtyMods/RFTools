package mcjty.rftools.blocks.booster;

import mcjty.lib.api.Infusable;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.teleporter.MatterTransmitterTileEntity;
import mcjty.rftools.network.PacketGetDestinationInfo;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.network.ReturnDestinationInfoHelper;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class BoosterBlock extends GenericRFToolsBlock<BoosterTileEntity, BoosterContainer> implements Infusable {

    public BoosterBlock() {
        super(Material.iron, BoosterTileEntity.class, BoosterContainer.class, "booster", false);
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiBooster.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(TextFormatting.WHITE + "This blocks gives entities directly on top of this");
            list.add(TextFormatting.WHITE + "block a temporary effect");
            list.add(TextFormatting.YELLOW + "Infusing bonus: reduced power consumption");
        } else {
            list.add(TextFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        return currenttip;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (heldItem != null && heldItem.getItem() instanceof EnvModuleProvider) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof BoosterTileEntity) {
                BoosterTileEntity boosterTileEntity = (BoosterTileEntity) te;
                if (boosterTileEntity.getStackInSlot(BoosterContainer.SLOT_MODULE) == null) {
                    ItemStack module = heldItem.copy();
                    module.stackSize = 1;
                    boosterTileEntity.setInventorySlotContents(BoosterContainer.SLOT_MODULE, module);
                    heldItem.stackSize--;
                    if (heldItem.stackSize == 0) {
                        player.setHeldItem(hand, null);
                    }
                    if (world.isRemote) {
                        player.addChatComponentMessage(new TextComponentString("Installed module in the booster"));
                    }
                    return true;
                }
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_BOOSTER;
    }
}
