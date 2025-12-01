package com.bocktom.worldEditEntities;

import com.bocktom.worldEditEntities.util.AsyncWorldEditScanUtil;
import com.bocktom.worldEditEntities.util.Config;
import com.bocktom.worldEditEntities.util.CountedMap;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.entity.EntityType;
import org.bukkit.Material;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Predicate;

import static com.bocktom.worldEditEntities.WorldEditEntitiesPlugin.plugin;

public class WorldEditHelper {

	public static CountedMap<EntityType> getEntitiesInSelection(Player player, Predicate<EntityType> filter) {
		Region selection = getSelection(player);
		if(selection == null) {
			return CountedMap.empty();
		}

		List<? extends Entity> entities = selection.getWorld().getEntities(selection);

		CountedMap<EntityType> map = new CountedMap<>();
		entities.stream()
				.filter(entity -> filter.test(entity.getType()))
				.forEach(entity -> map.increment(entity.getType()));

		return map.sortedByValueDescending();
	}

	public static CountedMap<String> getBlockTypesInSelection(Player player, Predicate<String> filter) {
		Region selection = getSelection(player);

		if (selection == null) {
			return CountedMap.empty();
		}

		CountedMap<String> map = new CountedMap<>();

		long start = System.currentTimeMillis();

		AsyncWorldEditScanUtil.startJob(player, selection, id -> {

			if(filter.test(id)) {
				map.increment(id);
			}
		}, () -> {
			long end = System.currentTimeMillis();
			double size = selection.getDimensions().length();
			plugin.getLogger().info("Completed job (" + String.format("%.2f", size) + " blocks in " + (end - start) + " ms)");
		});

		return map.sortedByValueDescending();
	}

	private static Region getSelection(Player player) {
		WorldEdit we = WorldEdit.getInstance();
		BukkitPlayer wePlayer = BukkitAdapter.adapt(player);
		LocalSession session = we.getSessionManager().getIfPresent(wePlayer);
		if(session == null) {
			return null;
		}

		if(session.getSelectionWorld() == null) {
			return null;
		}

		Region selection = session.getSelection();

		if(selection == null || selection.getWorld() == null)
			return null;
		return selection;
	}
}
