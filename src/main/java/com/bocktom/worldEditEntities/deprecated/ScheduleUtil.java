package com.bocktom.worldEditEntities.deprecated;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.World;
import org.bukkit.block.BlockState;

import java.util.*;
import java.util.function.Consumer;

public class ScheduleUtil {

	private static int TICKS_BETWEEN_BATCHES = 1;
	private static int BATCH_SIZE = 10; // chunks per batch
	private static Map<UUID, Job> jobs = new HashMap<>();

	private static class Job {
		public int taskId = 0;
		public UUID world;

		public int chunkIndex = 0;
		public List<BlockVector2> chunks;

		public Job(List<BlockVector2> chunks, UUID world, int taskId) {
			this.chunks = chunks;
			this.world = world;
			this.taskId = taskId;
		}
	}

	public static void startJob(UUID owner, Region selectedRegion, Consumer<BlockState> consumer, Runnable onComplete) {
		World world = BukkitAdapter.adapt(selectedRegion.getWorld());

		/*
		int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			Job job = jobs.get(owner);

			if(job.player == null) {
				// owner logged off
				Bukkit.getScheduler().cancelTask(job.taskId);
				jobs.remove(owner);
				return;
			}
			if (!job.chunkIterator.hasNext()) {
				// job is done
				Bukkit.getScheduler().cancelTask(job.taskId);
				jobs.remove(owner);
				onComplete.run();
				return;
			}


			// process a chunk
			BlockVector3 min = job.chunks.getMinimumPoint();
			BlockVector3 max = job.chunks.getMaximumPoint();

			// process up to BATCH_SIZE chunks per tick
			do {
				BlockVector2 chunk = job.chunkIterator.next();
				job.player.sendActionBar(Component.text("§7Processing chunks: §e" + job.chunksProcessed + " / " + job.chunks.getChunks().size()));

				job.world.getChunkAtAsync(chunk.x(), chunk.z()).thenAccept(loadedChunk -> {

					// Determine the area to process within this chunk
					int chunkMinX = chunk.x() << 4;
					int chunkMaxX = chunkMinX + 15;
					int chunkMinZ = chunk.z() << 4;
					int chunkMaxZ = chunkMinZ + 15;
					int minX = Math.max(chunkMinX, min.x()); // << 4
					int maxX = Math.min(chunkMaxX, max.x());
					int minZ = Math.max(chunkMinZ, min.z());
					int maxZ = Math.min(chunkMaxZ, max.z()); // | 15
					int minY = min.y();
					int maxY = max.y();

					//Bukkit.broadcastMessage("Processing chunk at (" + chunk.x() + ", " + chunk.z() + ") from (" + minX + ", " + minZ + ") to (" + maxX + ", " + maxZ + ")");

					// process chunk from (minX, minY, minZ) to (maxX, maxY, maxZ)
					for (int x = minX; x < maxX + 1; x++) {
						for (int y = minY; y < maxY + 1; y++) {
							for (int z = minZ; z < maxZ + 1; z++) {

								// chunk relative coordinates
								int blockX = x - chunkMinX;
								int blockZ = z - chunkMinZ;
								//Bukkit.broadcastMessage("Processing block at (" + x + ", " + z + ") Chunk rel (" + (blockX) + ", " + (blockZ) + ")" );

								Block block = loadedChunk.getBlock(blockX, y, blockZ);
								consumer.accept(block.getState());
							}
						}
					}
				}); // Performance heavy

				job.chunksProcessed++;
			} while (job.chunkIterator.hasNext() && job.chunksProcessed % BATCH_SIZE != 0);

		}, 0L, TICKS_BETWEEN_BATCHES).getTaskId();
		Job job = new Job(selectedRegion, world, Bukkit.getPlayer(owner), taskId);
		jobs.put(owner, job);
*/
	}

}
