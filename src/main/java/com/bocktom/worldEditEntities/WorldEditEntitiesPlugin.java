package com.bocktom.worldEditEntities;

import org.bukkit.plugin.java.JavaPlugin;

public final class WorldEditEntitiesPlugin extends JavaPlugin {

	public static WorldEditEntitiesPlugin plugin;

	@Override
	public void onEnable() {
		plugin = this;
		getCommand("/countentities").setExecutor(new CountEntitiesCommand());
	}

}
