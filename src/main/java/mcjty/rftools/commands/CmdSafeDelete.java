package mcjty.rftools.commands;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.teleporter.TeleportDestinations;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
        return DimletConfiguration.playersCanDeleteDimensions ? 0 : 3;
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

        if (!sender.canCommandSenderUseCommand(3, "safedel")) {
            DimensionInformation information = dimensionManager.getDimensionInformation(dim);
            if (information.getOwner() == null) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This dimension has no owner. You cannot delete it!"));
                return;
            }
            if (!(sender instanceof EntityPlayerMP)) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run as a player!"));
                return;
            }
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) sender;
            if (!information.getOwner().equals(entityPlayerMP.getGameProfile().getId())) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You are not the owner of this dimension. You cannot delete it!"));
                return;
            }
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

        if (DimletConfiguration.dimensionFolderIsDeletedWithSafeDel) {
            File rootDirectory = DimensionManager.getCurrentSaveRootDirectory();
            try {
                FileUtils.deleteDirectory(new File(rootDirectory.getPath() + File.separator + "DIM" + dim));
                sender.addChatMessage(new ChatComponentText("Dimension deleted and dimension folder succesfully wiped!"));
            } catch (IOException e) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Dimension deleted but dimension folder could not be completely wiped!"));
            }
        } else {
            sender.addChatMessage(new ChatComponentText("Dimension deleted. Please remove the dimension folder from disk!"));
        }
    }
}
