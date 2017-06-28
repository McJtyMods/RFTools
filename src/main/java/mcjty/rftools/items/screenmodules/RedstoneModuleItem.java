package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.logic.wireless.RedstoneReceiverTileEntity;
import mcjty.rftools.blocks.logic.wireless.RedstoneTransmitterTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.RedstoneScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.RedstoneClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class RedstoneModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public RedstoneModuleItem() {
        super("redstone_module");
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
    }


    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends IScreenModule> getServerScreenModule() {
        return RedstoneScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule> getClientScreenModule() {
        return RedstoneClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Red";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.REDSTONE_RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            int channel = tagCompound.getInteger("channel");
            if (channel != -1) {
                list.add(TextFormatting.YELLOW + "Channel: " + channel);
            } else if (tagCompound.hasKey("monitorx")) {
                int mx = tagCompound.getInteger("monitorx");
                int my = tagCompound.getInteger("monitory");
                int mz = tagCompound.getInteger("monitorz");
                list.add(TextFormatting.YELLOW + "Block at: " + mx + "," + my + "," + mz);
            }
        }
        list.add(TextFormatting.WHITE + "Sneak right-click on a redstone transmitter or");
        list.add(TextFormatting.WHITE + "receiver to set the channel for this module.");
        list.add(TextFormatting.WHITE + "Or else sneak right-click on the side of any");
        list.add(TextFormatting.WHITE + "block to monitor the redstone output on that side");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        int channel = -1;
        if (te instanceof RedstoneReceiverTileEntity) {
            channel = ((RedstoneReceiverTileEntity) te).getChannel();
        } else if (te instanceof RedstoneTransmitterTileEntity) {
            channel = ((RedstoneTransmitterTileEntity) te).getChannel();
        } else {
            // We selected a random block.
            tagCompound.setInteger("channel", -1);
            tagCompound.setInteger("monitordim", world.provider.getDimension());
            tagCompound.setInteger("monitorx", pos.getX());
            tagCompound.setInteger("monitory", pos.getY());
            tagCompound.setInteger("monitorz", pos.getZ());
            tagCompound.setInteger("monitorside", facing.ordinal());
            if (world.isRemote) {
                Logging.message(player, "Redstone module is set to " + pos);
            }

            return EnumActionResult.SUCCESS;
        }

        tagCompound.removeTag("monitordim");
        tagCompound.removeTag("monitorx");
        tagCompound.removeTag("monitory");
        tagCompound.removeTag("monitorz");
        tagCompound.removeTag("monitorside");

        if (channel != -1) {
            tagCompound.setInteger("channel", channel);
            if (world.isRemote) {
                Logging.message(player, "Redstone module is set to channel '" + channel + "'");
            }
        } else {
            tagCompound.removeTag("channel");
            if (world.isRemote) {
                Logging.message(player, "Redstone module is cleared");
            }
        }
        return EnumActionResult.SUCCESS;
    }
}