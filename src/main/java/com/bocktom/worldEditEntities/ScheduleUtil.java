package com.bocktom.worldEditEntities;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.bocktom.worldEditEntities.WorldEditEntitiesPlugin.plugin;

public class ScheduleUtil {

	private static int TICKS_BETWEEN_BATCHES = 1;
	private static int BATCH_SIZE = 10; // chunks per batch
	private static Map<UUID, Job> jobs = new HashMap<>();

	private static class Job {
		public int taskId = 0;
		public Iterator<BlockVector2> chunkIterator;
		public int chunksProcessed = 0;
		public Region selection;
		public World world;
		public Player player;

		public Job(Region selection, World world, Player player, int taskId) {
			this.selection = selection;
			this.world = world;
			this.player = player;
			this.taskId = taskId;
			this.chunkIterator = selection.getChunks().iterator();
		}
	}

	public static void startJob(UUID owner, Region selectedRegion, Consumer<BlockState> consumer, Runnable onComplete) {
		World world = BukkitAdapter.adapt(selectedRegion.getWorld());

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
			BlockVector3 min = job.selection.getMinimumPoint();
			BlockVector3 max = job.selection.getMaximumPoint();

			// process up to BATCH_SIZE chunks per tick
			do {
				BlockVector2 chunk = job.chunkIterator.next();
				job.player.sendActionBar(Component.text("§7Processing chunks: §e" + job.chunksProcessed + " / " + job.selection.getChunks().size()));

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
	}

}
