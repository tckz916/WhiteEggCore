package com.github.niwaniwa.we.core.command;

import org.bukkit.command.Command;

import com.github.niwaniwa.we.core.WhiteEggCore;
import com.github.niwaniwa.we.core.player.WhiteCommandSender;
import com.github.niwaniwa.we.core.util.message.MessageManager;

public abstract class AbstractWhiteEggCoreCommand extends AbstractWhiteEggCommand {

	protected final String commandPermission = "whiteegg.core.command";
	protected final String msgPrefix = "§7[§bWEC§7]§r";
	protected final String logPrefix = "[WEC]";

	public abstract boolean onCommand(WhiteCommandSender sender, Command cmd, String label, String[] args);

	protected abstract String description();

	public abstract String description(WhiteCommandSender sender);

	protected MessageManager msg = WhiteEggCore.getMessageManager();

}
