package com.bocktom.worldEditEntities.deprecated;


import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.List;
import java.util.function.Consumer;

public class Job {

	private static final int CHUNK_BATCH_SIZE = 10; // chunks per batch
	private static final int BLOCK_BATCH_SIZE = 10_000; // blocks per batch

	@Setter
	@Getter
	private int taskId = 0;
	private World world;

	@Getter
	private int chunkIndex = 0;
	@Getter
	private List<BlockVector2> chunks;
	private int blockIndex = 0;

	private BlockVector3 min;
	private BlockVector3 max;

	private Consumer<BlockState> consumer;
	private Runnable onComplete;

	public Job(List<BlockVector2> chunks, BlockVector3 min, BlockVector3 max, World world, Consumer<BlockState> consumer, Runnable onComplete, int taskId) {
		this.chunks = chunks;
		this.min = min;
		this.max = max;
		this.world = world;
		this.taskId = taskId;
		this.consumer = consumer;
		this.onComplete = onComplete;
	}

	public boolean step() {
		int batchesProcessed = 0;
		int blocksProcessed = 0;
		while (chunkIndex < chunks.size() && batchesProcessed < CHUNK_BATCH_SIZE && blocksProcessed < BLOCK_BATCH_SIZE) {

			BlockVector2 chunkPos = chunks.get(chunkIndex);
			Chunk chunk = world.getChunkAt(chunkPos.x(), chunkPos.z());

			// Determine the area to process within this chunk
			int chunkMinX = chunkPos.x() << 4;
			int chunkMaxX = chunkMinX + 15;
			int chunkMinZ = chunkPos.z() << 4;
			int chunkMaxZ = chunkMinZ + 15;
			int minX = Math.max(chunkMinX, min.x()); // << 4
			int maxX = Math.min(chunkMaxX, max.x());
			int minZ = Math.max(chunkMinZ, min.z());
			int maxZ = Math.min(chunkMaxZ, max.z()); // | 15
			int minY = min.y();
			int maxY = max.y();
			//Bukkit.broadcastMessage("Processing chunk at (" + chunk.x() + ", " + chunk.z() + ") from (" + minX + ", " + minZ + ") to (" + maxX + ", " + maxZ + ")");

			if(blockIndex > 0) {
				// skip already processed blocks in this chunk
				int blocksInChunk = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
				int blocksToSkip = blockIndex;
				int xRange = maxX - minX + 1;
				int yRange = maxY - minY + 1;
				int zRange = maxZ - minZ + 1;

				int skipX = blocksToSkip / (yRange * zRange);
				blocksToSkip -= skipX * yRange * zRange;
				int skipY = blocksToSkip / zRange;
				blocksToSkip -= skipY * zRange;
				int skipZ = blocksToSkip;

				minX += skipX;
				minY += skipY;
				minZ += skipZ;

				//Bukkit.broadcastMessage("Resuming at (" + minX + ", " + minY + ", " + minZ + ")");
			}

			// process chunk from (minX, minY, minZ) to (maxX, maxY, maxZ)
			for (int x = minX; x < maxX + 1; x++) {
				for (int y = minY; y < maxY + 1; y++) {
					for (int z = minZ; z < maxZ + 1; z++) {

						if(blocksProcessed >= BLOCK_BATCH_SIZE) {
							// reached block limit for this batch
							return true;
						}

						// chunk relative coordinates
						int blockX = x - chunkMinX;
						int blockZ = z - chunkMinZ;
						//Bukkit.broadcastMessage("Processing block at (" + x + ", " + z + ") Chunk rel (" + (blockX) + ", " + (blockZ) + ")" );

						Block block = chunk.getBlock(blockX, y, blockZ);
						consumer.accept(block.getState());

						blockIndex++;
						blocksProcessed++;
					}
				}
			}

			blockIndex = 0;
			chunkIndex++;
			batchesProcessed++;
		}

		if(chunkIndex < chunks.size()) {
			return true; // more to process
		} else {
			onComplete.run();
			cancel();
			return false; // done
		}
	}

	public void cancel() {
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
