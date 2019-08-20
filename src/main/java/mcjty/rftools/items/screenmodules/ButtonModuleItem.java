package mcjty.rftools.items.screenmodules;

import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ButtonScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ButtonClientScreenModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ButtonModuleItem extends Item implements IModuleProvider {

    public ButtonModuleItem() {
        super(new Properties()
                .defaultMaxDamage(1)
                .group(RFTools.setup.getTab()));
        setRegistryName("button_module");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<ButtonScreenModule> getServerScreenModule() {
        return ButtonScreenModule.class;
    }

    @Override
    public Class<ButtonClientScreenModule> getClientScreenModule() {
        return ButtonClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Button";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Label color").nl()
                .label("Button:").text("button", "Button text").color("buttonColor", "Button color").nl()
                .toggle("toggle", "Toggle", "Toggle button mode")
                .choices("align", "Label alignment", "Left", "Center", "Right").nl();

    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, world, list, advanced);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.BUTTON_RFPERTICK.get() + " RF/tick"));
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text")));
            int channel = tagCompound.getInt("channel");
            if (channel != -1) {
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Channel: " + channel));
            }
        }
        list.add(new StringTextComponent(TextFormatting.WHITE + "Sneak right-click on a redstone receiver"));
        list.add(new StringTextComponent(TextFormatting.WHITE + "to create a channel for this module and also"));
        list.add(new StringTextComponent(TextFormatting.WHITE + "set it to the receiver. You can also use this"));
        list.add(new StringTextComponent(TextFormatting.WHITE + "on a transmitter or already set receiver to copy"));
        list.add(new StringTextComponent(TextFormatting.WHITE + "the channel to the button"));
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }
}