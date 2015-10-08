package com.github.niwaniwa.we.core.player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AltAccount implements ConfigurationSerializable {

	private final List<String> players;

	public AltAccount() {
		this.players = new ArrayList<>();
	}

	public List<String> get(){
		return players;
	}

	public boolean add(UUID player){
		return players.add(player.toString());
	}

	public boolean contains(WhitePlayer player){
		return players.contains(player.getUniqueId().toString());
	}

	public boolean remove(Object o){
		return players.remove(o);
	}

	public static void determine(WhitePlayer player) {
		if(!(player instanceof WhiteEggPlayer)){ return; }
		WhiteEggPlayer egg = (WhiteEggPlayer) player;
		for(WhitePlayer p : WhitePlayerFactory.getPlayers()){
			if(!(p instanceof WhiteEggPlayer)){ continue; }
			WhiteEggPlayer egg2 = (WhiteEggPlayer) p;
			if(getAddress(egg.getAddress()).equalsIgnoreCase(getAddress(egg2.getAddress()))){
				egg.addAccount(p);
				egg2.addAccount(egg);
			}
		}
	}

	private static String getAddress(InetSocketAddress address){
		String[] loginAdress = address.getAddress().toString().split("/");
		loginAdress = loginAdress[1].split(":");
		return loginAdress[0];
	}

	@Override
	public String toString() {
		return this.serialize().toString();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialize = new HashMap<>();
		serialize.put("player", players);
		return null;
	}

	public static AltAccount parser(JSONObject json){
		AltAccount alt = new AltAccount();
		Object obj = json.get("account");
		if(obj == null
				|| (obj instanceof JSONArray)){ return alt; }
		JSONArray array = JSONArray.fromObject(obj);
		for(Object o : array){
			alt.add(UUID.fromString(String.valueOf(o)));
		}
		return alt;
	}

}