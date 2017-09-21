package mcjty.rftools.commands;

import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
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
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Too many parameters!"));
            return;
        }
        if (args.length < 2) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Missing filename!"));
            return;
        }

        if (!(sender instanceof EntityPlayer)) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "This command only works as a player!"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!ItemStackTools.isValid(heldItem) || !(heldItem.getItem() instanceof ShapeCardItem)) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "You need to hold a shapecard in your hand!"));
            return;
        }

        ShapeCardItem.save(player, heldItem, args[1]);
    }
}
