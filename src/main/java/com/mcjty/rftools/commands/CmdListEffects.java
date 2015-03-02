package com.mcjty.rftools.commands;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.world.types.EffectType;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdListEffects extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "effectlist";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;

            int dim = player.worldObj.provider.dimensionId;
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
            DimensionInformation information = dimensionManager.getDimensionInformation(dim);
            if (information == null) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
                return;
            }
            for (EffectType type : information.getEffectTypes()) {
                sender.addChatMessage(new ChatComponentText("    Effect: " + type.toString().substring(7)));
            }
        }
    }
}
