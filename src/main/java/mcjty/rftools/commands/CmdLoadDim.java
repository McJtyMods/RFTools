package mcjty.rftools.commands;

import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class CmdLoadDim extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "<dimension> <filename>";
    }

    @Override
    public String getCommand() {
        return "loaddim";
    }

    @Override
    public int getPermissionLevel() {
        return 2;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "The dimension and filename parameters are missing!"));
            return;
        } else if (args.length > 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Too many parameters!"));
            return;
        }

        int dim = fetchInt(sender, args, 1, 0);
        String filename = fetchString(sender, args, 2, null);

        World world = sender.getEntityWorld();

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
        if (dimensionManager.getDimensionDescriptor(dim) == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Not an RFTools dimension!"));
            return;
        }

        DimensionInformation information = dimensionManager.getDimensionInformation(dim);
        String error = information.loadFromJson(filename);
        if (error != null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Error: "+ error));
        } else {
            dimensionManager.save(world);
        }
    }
}
