package com.github.niwaniwa.we.core.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.niwaniwa.we.core.WhiteEggCore;
import com.github.niwaniwa.we.core.event.WhiteEggTweetEvent;

public class Debug implements Listener {

	/** debag **/
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		if(event.getPlayer().getUniqueId().toString()
				.equalsIgnoreCase("f010845c-a9ac-4a04-bf27-61d92f8b03ff")){
			WhiteEggCore.getInstance().getLogger().info(
					"-- " + event.getPlayer().getName() + "Join the game. --");
		}
	}

	@EventHandler
	public void onTweet(WhiteEggTweetEvent event){
//		Logger l = WhiteEggCore.getInstance().getLogger();
//		l.info("Tweet : " + event.getTweet());
	}

}
