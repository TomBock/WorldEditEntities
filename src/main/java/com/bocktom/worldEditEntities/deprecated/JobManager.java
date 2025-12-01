package com.bocktom.worldEditEntities.deprecated;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class JobManager {

	private static final int TICK_SPEED = 1;
	private static final Map<UUID, Job> jobs = new HashMap<>();

	public static void startJob(Player player, Region selection, Consumer<BlockState> consumer, Runnable onComplete) {
		World world = BukkitAdapter.adapt(selection.getWorld());
		UUID owner = player.getUniqueId();

		// Cancel existing job if one exists
		if(jobs.containsKey(player.getUniqueId())) {
			Job existingJob = jobs.get(owner);
			Bukkit.getScheduler().cancelTask(existingJob.getTaskId());
			jobs.remove(owner);
		}

		HashSet<String> ids = new HashSet<>();
		for (BlockVector3 vec : selection) {
			com.sk89q.worldedit.world.block.BlockState state = vec.getBlock(selection.getWorld());
			ids.add(state.getBlockType().id());
		}
		for (String id : ids) {
			Bukkit.broadcastMessage("Found block: " + id);
		}

		/*

		Job job = new Job(new ArrayList<>(
				selection.getChunks()),
				selection.getMinimumPoint(),
				selection.getMaximumPoint(),
				world,
				consumer,
				onComplete,
				0);

		job.setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, () -> {

			Job myJob = jobs.get(owner);
			if(!player.isOnline()) {
				myJob.cancel();
				jobs.remove(owner);
				return;
			}

			player.sendActionBar(Component.text("§7Processing chunks: §e" + myJob.getChunkIndex() + " / " + myJob.getChunks().size()));

			// Processing
			if(!myJob.step()) {

				// job is done
				jobs.remove(owner);
			}

		}, 1L, TICK_SPEED).getTaskId());
		jobs.put(owner, job);*/
	}
}
