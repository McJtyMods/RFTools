package com.mcjty.rftools.commands;

import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class CmdSafeDelete extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension>";
    }

    @Override
    public String getCommand() {
        return "safedel";
    }

    @Override
    public int getPermissionLevel() {
        return 3;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The dimension parameter is missing!"));
            return;
        } else if (args.length > 2) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1, 0);
        World world = sender.getEntityWorld();

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
        if (dimensionManager.getDimensionDescriptor(dim) == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
            return;
        }

        World w = DimensionManager.getWorld(dim);
        if (w != null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dimension is still in use!"));
            return;
        }

        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.removeDestinationsInDimension(dim);
        destinations.save(world);

        dimensionManager.removeDimension(dim);
        dimensionManager.reclaimId(dim);
        dimensionManager.save(world);

        DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(world);
        dimensionStorage.removeDimension(dim);
        dimensionStorage.save(world);

        sender.addChatMessage(new ChatComponentText("Dimension deleted. Please remove the dimension folder from disk!"));
    }
}
