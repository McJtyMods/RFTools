package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.CapabilityTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.FluidPlusBarScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.FluidPlusBarClientScreenModule;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class FluidPlusModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public FluidPlusModuleItem() {
        super("fluidplus_module");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<FluidPlusBarScreenModule> getServerScreenModule() {
        return FluidPlusBarScreenModule.class;
    }

    @Override
    public Class<FluidPlusBarClientScreenModule> getClientScreenModule() {
        return FluidPlusBarClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Fluid";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("mb+:").color("rfcolor", "Color for the mb text").label("mb-:").color("rfcolor_neg", "Color for the negative", "mb/tick ratio").nl()
                .toggleNegative("hidebar", "Bar", "Toggle visibility of the", "fluid bar").mode("mb").format("format").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").nl()
                .label("Block:").block("monitor").nl();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.FLUIDPLUS_RFPERTICK.get() + " RF/tick");
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            if (tagCompound.hasKey("monitorx")) {
                int dim;
                if (tagCompound.hasKey("monitordim")) {
                    dim = tagCompound.getInteger("monitordim");
                } else {
                    // Compatibility reasons
                    dim = tagCompound.getInteger("dim");
                }
                int monitorx = tagCompound.getInteger("monitorx");
                int monitory = tagCompound.getInteger("monitory");
                int monitorz = tagCompound.getInteger("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")");
                list.add(TextFormatting.YELLOW + "Dimension: " + dim);
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(TextFormatting.YELLOW + "Sneak right-click on a tank to set the");
            list.add(TextFormatting.YELLOW + "target for this fluid module");
        }
    }

    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        if (CapabilityTools.hasFluidCapabilitySafe(te) != null) {
            tagCompound.setInteger("monitordim", world.provider.getDimension());
            tagCompound.setInteger("monitorx", pos.getX());
            tagCompound.setInteger("monitory", pos.getY());
            tagCompound.setInteger("monitorz", pos.getZ());
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockTools.getReadableName(world, pos);
            }
            tagCompound.setString("monitorname", name);
            if (world.isRemote) {
                Logging.message(player, "Fluid module is set to block '" + name + "'");
            }
        } else {
            tagCompound.removeTag("monitordim");
            tagCompound.removeTag("monitorx");
            tagCompound.removeTag("monitory");
            tagCompound.removeTag("monitorz");
            tagCompound.removeTag("monitorname");
            if (world.isRemote) {
                Logging.message(player, "Fluid module is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return ActionResultType.SUCCESS;
    }
}