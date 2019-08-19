package mcjty.rftools.commands;

import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CmdSaveCard extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<filename>";
    }

    @Override
    public String getCommand() {
        return "save";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length > 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Missing filename!"));
            return;
        }

        if (!(sender instanceof PlayerEntity)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "This command only works as a player!"));
            return;
        }

        PlayerEntity player = (PlayerEntity) sender;
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ShapeCardItem)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "You need to hold a shapecard in your hand!"));
            return;
        }

        ShapeCardItem.save(player, heldItem, args[1]);
    }
}
