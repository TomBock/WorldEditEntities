package com.bocktom.worldEditEntities.util;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class AsyncWorldEditScanUtil {


	public static void startJob(Player player, Region selection, Consumer<String> consumer, Runnable onComplete) {

		for (BlockVector3 vec : selection) {
			com.sk89q.worldedit.world.block.BlockState state = vec.getBlock(selection.getWorld());
			consumer.accept(state.getBlockType().id());
		}
		onComplete.run();
	}

}
