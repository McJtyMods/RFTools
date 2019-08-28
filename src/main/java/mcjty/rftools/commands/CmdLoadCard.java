package mcjty.rftools.commands;

import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CmdLoadCard extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<filename>";
    }

    @Override
    public String getCommand() {
        return "load";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(PlayerEntity sender, String[] args) {
        if (args.length > 2) {
            sender.sendMessage(new StringTextComponent(TextFormatting.RED + "Too many parameters!"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new StringTextComponent(TextFormatting.RED + "Missing filename!"));
            return;
        }

        if (sender == null) {
            sender.sendMessage(new StringTextComponent(TextFormatting.RED + "This command only works as a player!"));
            return;
        }

        ItemStack heldItem = sender.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ShapeCardItem)) {
            sender.sendMessage(new StringTextComponent(TextFormatting.RED + "You need to hold a shapecard in your hand!"));
            return;
        }

        ShapeCardItem.load(sender, heldItem, args[1]);
    }
}
