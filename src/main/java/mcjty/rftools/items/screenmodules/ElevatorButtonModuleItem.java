package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.Logging;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.elevator.ElevatorTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ElevatorButtonScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ElevatorButtonClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ElevatorButtonModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public ElevatorButtonModuleItem() {
        super("elevator_button_module");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends IScreenModule> getServerScreenModule() {
        return ElevatorButtonScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule> getClientScreenModule() {
        return ElevatorButtonClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "EButton";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.ELEVATOR_BUTTON_RFPERTICK + " RF/tick");
        boolean hasTarget = false;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            if (tagCompound.hasKey("elevatorx")) {
                int monitorx = tagCompound.getInteger("elevatorx");
                int monitory = tagCompound.getInteger("elevatory");
                int monitorz = tagCompound.getInteger("elevatorz");
                String monitorname = tagCompound.getString("elevatorname");
                list.add(TextFormatting.YELLOW + "Elevator: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")");
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(TextFormatting.YELLOW + "Sneak right-click on an elector block to set the");
            list.add(TextFormatting.YELLOW + "target for this module");
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (te instanceof ElevatorTileEntity) {
            tagCompound.setInteger("elevatordim", world.provider.getDimension());
            tagCompound.setInteger("elevatorx", pos.getX());
            tagCompound.setInteger("elevatory", pos.getY());
            tagCompound.setInteger("elevatorz", pos.getZ());
            IBlockState state = player.getEntityWorld().getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockInfo.getReadableName(world.getBlockState(pos));
            }
            tagCompound.setString("elevatorname", name);
            if (world.isRemote) {
                Logging.message(player, "Elevator module is set to block '" + name + "'");
            }
        } else {
            tagCompound.removeTag("elevatordim");
            tagCompound.removeTag("elevatorx");
            tagCompound.removeTag("elevatory");
            tagCompound.removeTag("elevatorz");
            tagCompound.removeTag("elevatorname");
            if (world.isRemote) {
                Logging.message(player, "Elevator module is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return EnumActionResult.SUCCESS;
    }
}