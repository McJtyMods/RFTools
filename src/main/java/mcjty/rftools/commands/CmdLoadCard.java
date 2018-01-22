package mcjty.rftools.commands;

import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
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
    public void execute(ICommandSender sender, String[] args) {
        if (args.length > 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Missing filename!"));
            return;
        }

        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "This command only works as a player!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof ShapeCardItem)) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "You need to hold a shapecard in your hand!"));
            return;
        }

        ShapeCardItem.load(player, heldItem, args[1]);
    }
}
