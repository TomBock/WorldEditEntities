package com.bocktom.worldEditEntities;

import com.bocktom.worldEditEntities.util.ChatUtil;
import com.bocktom.worldEditEntities.util.Config;
import com.bocktom.worldEditEntities.util.CountedMap;
import com.bocktom.worldEditEntities.util.FilterUtil;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.registry.EntityRegistry;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static com.bocktom.worldEditEntities.util.FilterUtil.getFilter;

public class CountEntitiesCommand implements CommandExecutor, TabCompleter {


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}

		switch (cmd.getName()) {
			case "/countentities" -> count(player, args, true);
			case "/counttileentities" -> count(player, args, false);
			case "/countall" -> {
				count(player, args, true);
				count(player, args, false);
			}
			case "/countstop" -> {
				AsyncWorldEditHelper.cancelBlockScanTask(player);
				sender.sendMessage("§7Cancelled any ongoing block scan tasks.");
			}
			default -> {
				sender.sendMessage("Unknown command.");
				return true;
			}
		}

		return true;
	}

	private void count(Player player, String[] args, boolean entity) {

		if(entity) {
			countEntities(player, args);
		} else {
			countTileEntities(player, args);
		}
	}

	private void countEntities(Player player, String[] args) {

		Predicate<EntityType> entityFilter = getFilter(args, FilterUtil::getEntityFilter);
		AsyncWorldEditHelper.countEntitiesAsync(player, entityFilter)
				.thenAccept(map -> {
					if(!player.isOnline())
						return;

					if(map.isEmpty()) {
						player.sendMessage("§cNo entities found in the selected region or no region selected.");
					} else {
						map.keyFormatter = type -> type.getName().replace("minecraft:", "");
						ChatUtil.sendTable(player, map, "Entities", args);
					}
				});
	}

	private void countTileEntities(Player player, String[] args) {

		Predicate<String> defaultBlockFilter = FilterUtil.getBlockFilter("tile_entities"); // always active
		Predicate<String> blockFilter = defaultBlockFilter.and(getFilter(args, FilterUtil::getBlockFilter));
		AsyncWorldEditHelper.countBlockTypesAsync(player, blockFilter)
				.thenAccept(map -> {
					if(!player.isOnline())
						return;

					if(map.isEmpty()) {
						player.sendMessage("§cNo blocks found in the selected region or no region selected.");
					} else {
						map.keyFormatter = id -> id.replace("minecraft:", "");
						ChatUtil.sendTable(player, map, "Tile Entities", args);
					}
				});
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
		if(cmd.getName().equals("/countentities")) {
			return getEntityTypeCompletions(args);
		} else if(cmd.getName().equals("/counttileentities")) {
			return getTileEntityCompletions(args);
		} else if(cmd.getName().equals("/countall")) {
			List<String> list = new ArrayList<>(getEntityTypeCompletions(args));
			list.addAll(getTileEntityCompletions(args));
			return list;
		}
		return List.of();
	}

	private List<String> getEntityTypeCompletions(@NotNull String[] args) {
		return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE)
				.stream()
				.map(type -> type.name().replace("minecraft:", "").toLowerCase(Locale.ROOT))
				.toList();
	}

	private List<String> getTileEntityCompletions(@NotNull String[] args) {
		List<String> autoComplete = new ArrayList<>();
		Config.tileEntities.get.getStringList("tile_entities")
				.stream()
				.map(id -> id.replace("minecraft:", ""))
				.forEach(autoComplete::add);
		autoComplete.addFirst("container");
		return autoComplete;
	}
}
