package net.coreprotect.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.lookup.ChestTransactionLookup;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DumpLogsCommand {
    protected static void runCommand(final CommandSender sender, boolean permission, String[] args) {
        if (!permission) {
            Chat.sendMessage(sender,
                Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
            return;
        }
        if (!(sender instanceof Player)) {
            Chat.sendMessage(sender, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- Needs to run as player");
            return;
        }
        if (ConfigHandler.converterRunning) {
            Chat.sendMessage(sender,
                Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
            return;
        }
        if (ConfigHandler.purgeRunning) {
            Chat.sendMessage(sender,
                Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
            return;
        }

        if (args.length == 5) {
            int posX = Integer.parseInt(args[1]);
            int posY = Integer.parseInt(args[2]);
            int posZ = Integer.parseInt(args[3]);
            int page = Integer.parseInt(args[4]);

            final var player = ((Player) sender);
            final var pos = new Location(player.getWorld(), posX, posY, posZ);

            new Thread(() -> {
                try (Connection connection = Database.getConnection(true)) {
                    if (connection != null) {
                        Statement statement = connection.createStatement();
                        ChestTransactionLookup.performLookup(null, statement, pos, player, page, 1000, true);
                    }
                    ConfigHandler.lookupThrottle.put(sender.getName(), new Object[]{false, System.currentTimeMillis()});
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).start();
            return;
        }

        Chat.sendMessage(sender,
            Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS,
                Color.WHITE, "/co dump-logs [x] [y] [z] [page]"));
    }
}
