package mcjty.rftools.commands;

import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.dimlets.*;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class CmdDimletCfg extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "dimletcfg";
    }

    @Override
    public int getPermissionLevel() {
        return 1;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        World world = sender.getEntityWorld();
        ItemStack heldItem = null;
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            heldItem = player.getHeldItem();
        }
        if (heldItem == null || heldItem.getItem() != ModItems.knownDimlet) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to hold a known dimlet in your hand!"));
            return;
        }

        DimletKey key = KnownDimletConfiguration.getDimletKey(heldItem, world);
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry != null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "In dimlets.cfg:"));
            sender.addChatMessage(new ChatComponentText("dimletsettings {"));
            DimletType type = key.getType();
            sender.addChatMessage(new ChatComponentText("    I:\"rarity." + type.dimletType.getName()+"."+key.getName() + "\"=" + entry.getRarity()));
            sender.addChatMessage(new ChatComponentText("    I:\"rfcreate." + type.dimletType.getName()+"."+key.getName() + "\"=" + entry.getRfCreateCost()));
            sender.addChatMessage(new ChatComponentText("    I:\"rfmaintain." + type.dimletType.getName()+"."+key.getName() + "\"=" + entry.getRfMaintainCost()));
            sender.addChatMessage(new ChatComponentText("    I:\"ticks." + type.dimletType.getName()+"."+key.getName() + "\"=" + entry.getTickCost()));
            if (entry.isRandomNotAllowed()) {
                sender.addChatMessage(new ChatComponentText("    B:\"expensive." + type.dimletType.getName()+"."+key.getName() + "\"=true"));
            }
            sender.addChatMessage(new ChatComponentText("}"));
            if (!entry.isRandomNotAllowed()) {
                if (type == DimletType.DIMLET_MATERIAL || type == DimletType.DIMLET_LIQUID) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "If you only want this for features (no terrain):"));
                    sender.addChatMessage(new ChatComponentText("    B:\"expensive." + type.dimletType.getName() + "." + key.getName() + "\"=true"));
                } else {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "If you don't want this dimlet to be generated random:"));
                    sender.addChatMessage(new ChatComponentText("    B:\"expensive." + type.dimletType.getName() + "." + key.getName() + "\"=true"));
                }
            }
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "In dimlets.cfg, if you want to blacklist this dimlet:"));
            sender.addChatMessage(new ChatComponentText("knowndimlets {"));
            sender.addChatMessage(new ChatComponentText("    I:\"dimlet." + type.dimletType.getName()+"."+key.getName() + "\"=-1"));
            sender.addChatMessage(new ChatComponentText("}"));
            String modid = null;
            if (type == DimletType.DIMLET_MATERIAL) {
                BlockMeta blockMeta = DimletObjectMapping.idToBlock.get(key);
                if (blockMeta != null) {
                    modid = KnownDimletConfiguration.getModidForBlock(blockMeta.getBlock());
                }
            } else if (type == DimletType.DIMLET_LIQUID) {
                Block block = DimletObjectMapping.idToFluid.get(key);
                if (block != null) {
                    modid = KnownDimletConfiguration.getModidForBlock(block);
                }
            }
            if (modid != null && !"?".equals(modid)) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "In dimlets.cfg, if you want to blacklist the entire mod:"));
                sender.addChatMessage(new ChatComponentText("knowndimlets {"));
                sender.addChatMessage(new ChatComponentText("    B:\"modban." + type.dimletType.getName() + "." + modid + "\"=true"));
                sender.addChatMessage(new ChatComponentText("}"));
            }
        }
    }
}
