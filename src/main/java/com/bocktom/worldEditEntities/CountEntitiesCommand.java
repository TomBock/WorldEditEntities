package com.bocktom.worldEditEntities;

import com.bocktom.worldEditEntities.util.ChatUtil;
import com.bocktom.worldEditEntities.util.CountedMap;
import com.bocktom.worldEditEntities.util.FilterUtil;
import com.sk89q.worldedit.world.entity.EntityType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static com.bocktom.worldEditEntities.util.FilterUtil.getFilter;

public class CountEntitiesCommand implements CommandExecutor, TabCompleter {


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}


		// Entities
		String filterRaw = args.length > 0 ? args[0] : ""; // empty filter are skipped for entities
		Predicate<EntityType> entityFilter = getFilter(filterRaw, FilterUtil::getEntityFilter);
		CountedMap<EntityType> entityCounts = WorldEditHelper.getEntitiesInSelection(player, entityFilter);

		if(entityCounts.isEmpty()) {
			player.sendMessage("§cNo entities found in the selected region or no region selected.");
		} else {
			ChatUtil.sendTable(player, "Entities", entityCounts, type -> type.getName().replace("minecraft:", ""));
		}

		// Tile Entities
		Predicate<String> defaultBlockFilter = FilterUtil.getBlockFilter("tile_entities"); // always active
		Predicate<String> blockFilter = defaultBlockFilter.and(getFilter(filterRaw, FilterUtil::getBlockFilter));
		CountedMap<String> blockCounts = WorldEditHelper.getBlockTypesInSelection(player, blockFilter);
		if(blockCounts.isEmpty()) {
			player.sendMessage("§cNo blocks found in the selected region or no region selected.");
		} else {
			ChatUtil.sendTable(player, "Tile Entities", blockCounts, type -> type.replace("minecraft:", ""));
		}

		return true;
	}


	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
		return List.of();
	}
}
