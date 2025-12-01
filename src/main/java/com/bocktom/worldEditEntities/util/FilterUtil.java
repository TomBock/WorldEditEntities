package com.bocktom.worldEditEntities.util;

import com.sk89q.worldedit.world.entity.EntityType;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

public class FilterUtil {

	public static @NotNull Predicate<EntityType> getEntityFilter(String filterRaw) {
		return switch (filterRaw.toLowerCase(Locale.ROOT)) {
			case "mobs" -> type -> type instanceof Mob;
			case "items" -> type -> type instanceof Item;
			default -> type -> type.getName().replace("minecraft:", "").equalsIgnoreCase(filterRaw);
		};
	}

	public static @NotNull Predicate<String> getBlockFilter(String filterRaw) {
		// Check for custom list
		List<String> customList = Config.tileEntities.get.getStringList(filterRaw);
		if(!customList.isEmpty()) {
			return customList::contains;
		}

		// Check for specific types
		return id -> id.equals(filterRaw);
	}

	public static <T> Predicate<T> getFilter(String filters, Function<String, Predicate<T>> getFilterFunction) {
		Predicate<T> filterFull = t -> false;

		if(filters.isBlank()) {
			filterFull = t -> true;
			return filterFull;
		}

		for (String filterRaw : filters.split(",")) {
			if(filterRaw.isBlank()) {
				continue;
			}

			Predicate<T> filter;
			boolean reversed = false;
			if(filterRaw.startsWith("!")) {
				reversed = true;
				filterRaw = filterRaw.substring(1);
			}
			filter = getFilterFunction.apply(filterRaw);
			filter = reversed ? filter.negate() : filter;
			filterFull = filterFull.or(filter);
		}

		return filterFull;
	}
}
