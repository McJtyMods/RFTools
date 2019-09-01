package mcjty.rftools.items.screenmodules;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.MachineInformationScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.MachineInformationClientScreenModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class MachineInformationModuleItem extends Item implements IModuleProvider {

    public MachineInformationModuleItem() {
        super(new Item.Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("machineinformation_module");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<MachineInformationScreenModule> getServerScreenModule() {
        return MachineInformationScreenModule.class;
    }

    @Override
    public Class<MachineInformationClientScreenModule> getClientScreenModule() {
        return MachineInformationClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Info";
    }

    private static final IModuleGuiBuilder.Choice[] EMPTY_CHOICES = new IModuleGuiBuilder.Choice[0];

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        World world = guiBuilder.getWorld();
        CompoundNBT currentData = guiBuilder.getCurrentData();
        IModuleGuiBuilder.Choice[] choices = EMPTY_CHOICES;
        if((currentData.contains("monitordim") ? currentData.getInt("monitordim") : currentData.getInt("dim")) == world.getDimension().getType().getId()) {
	        TileEntity tileEntity = world.getTileEntity(new BlockPos(currentData.getInt("monitorx"), currentData.getInt("monitory"), currentData.getInt("monitorz")));
	        if (tileEntity instanceof MachineInformation) {
	            MachineInformation information = (MachineInformation)tileEntity;
	            int count = information.getTagCount();
	            choices = new IModuleGuiBuilder.Choice[count];
	            for (int i = 0; i < count; ++i) {
	                choices[i] = new IModuleGuiBuilder.Choice(information.getTagName(i), information.getTagDescription(i));
	            }
	        }
        }

        guiBuilder
                .label("L:").color("color", "Color for the label").label("Txt:").color("txtcolor", "Color for the text").nl()
                .choices("monitorTag", choices).nl()
                .block("monitor").nl();
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.MACHINEINFO_RFPERTICK.get() + " RF/tick"));
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text")));
            if (tagCompound.contains("monitorx")) {
                int monitorx = tagCompound.getInt("monitorx");
                int monitory = tagCompound.getInt("monitory");
                int monitorz = tagCompound.getInt("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")"));
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Sneak right-click on a supported machine"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "to set the target for this module"));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        PlayerEntity player = context.getPlayer();
        TileEntity te = world.getTileEntity(pos);
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        if (te instanceof MachineInformation) {
            tagCompound.putInt("monitordim", world.getDimension().getType().getId());
            tagCompound.putInt("monitorx", pos.getX());
            tagCompound.putInt("monitory", pos.getY());
            tagCompound.putInt("monitorz", pos.getZ());
            BlockState state = player.getEntityWorld().getBlockState(pos);
            Block block = state.getBlock();
            String name = "<invalid>";
            if (block != null && !block.isAir(state, world, pos)) {
                name = BlockTools.getReadableName(world, pos);
            }
            tagCompound.putString("monitorname", name);
            if (world.isRemote) {
                Logging.message(player, "Machine Information module is set to block '" + name + "'");
            }
        } else {
            tagCompound.remove("monitordim");
            tagCompound.remove("monitorx");
            tagCompound.remove("monitory");
            tagCompound.remove("monitorz");
            tagCompound.remove("monitorname");
            if (world.isRemote) {
                Logging.message(player, "Machine Information module is cleared");
            }
        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }
}