package mcjty.rftools.items.screenmodules;

import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.elevator.ElevatorTileEntity;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ElevatorButtonScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ElevatorButtonClientScreenModule;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ElevatorButtonModuleItem extends Item implements IModuleProvider {

    public ElevatorButtonModuleItem() {
        super(new Properties().defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("elevator_button_module");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<ElevatorButtonScreenModule> getServerScreenModule() {
        return ElevatorButtonScreenModule.class;
    }

    @Override
    public Class<ElevatorButtonClientScreenModule> getClientScreenModule() {
        return ElevatorButtonClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "EButton";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .color("buttonColor", "Button color").color("curColor", "Current level button color").nl()
                .toggle("vertical", "Vertical", "Order the buttons vertically").toggle("large", "Large", "Larger buttons").nl()
                .toggle("lights", "Lights", "Use buttons resembling lights").toggle("start1", "Start 1", "start numbering at 1 instead of 0").nl()
                .text("l0", "Level 0 name").text("l1", "Level 1 name").text("l2", "Level 2 name").text("l3", "Level 3 name").nl()
                .text("l4", "Level 4 name").text("l5", "Level 5 name").text("l6", "Level 6 name").text("l7", "Level 7 name").nl()
                .label("Block:").block("elevator").nl();
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.ELEVATOR_BUTTON_RFPERTICK.get() + " RF/tick"));
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            if (tagCompound.contains("elevatorx")) {
                int monitorx = tagCompound.getInt("elevatorx");
                int monitory = tagCompound.getInt("elevatory");
                int monitorz = tagCompound.getInt("elevatorz");
                String monitorname = tagCompound.getString("elevatorname");
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Elevator: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")"));
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Sneak right-click on an elevator block to set the"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "target for this module"));
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
        if (te instanceof ElevatorTileEntity) {
            tagCompound.setInteger("elevatordim", world.provider.getDimension());
            tagCompound.setInteger("elevatorx", pos.getX());
            tagCompound.setInteger("elevatory", pos.getY());
            tagCompound.setInteger("elevatorz", pos.getZ());
            BlockState state = player.getEntityWorld().getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockTools.getReadableName(world, pos);
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
        return ActionResultType.SUCCESS;
    }
}