package cn.valorin.dueltime.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class SafeLocationFinder {
    public static Location findNearestSafeLocation(Location originalLocation) {
        if (originalLocation == null) {
            // 如果原始位置为空，返回默认位置
            return new Location(null, 0, 100, 0, 0, 0);
        }
        
        World world = originalLocation.getWorld();
        if (world == null) {
            // 如果世界为空，返回原始位置副本（如果可能的话）
            return new Location(null, originalLocation.getX(), originalLocation.getY(), originalLocation.getZ(), 
                               originalLocation.getYaw(), originalLocation.getPitch());
        }

        float yaw = originalLocation.getYaw();
        float pitch = originalLocation.getPitch();

        PriorityQueue<BlockPosition> queue = new PriorityQueue<>(Comparator.comparingDouble(b ->
                distanceSquared(b, originalLocation)
        ));
        Set<BlockPosition> visited = new HashSet<>();

        BlockPosition initialFoot = new BlockPosition(
                (int) Math.floor(originalLocation.getX()),
                (int) Math.floor(originalLocation.getY()) - 1,
                (int) Math.floor(originalLocation.getZ())
        );
        queue.add(initialFoot);
        visited.add(initialFoot);

        while (!queue.isEmpty()) {
            BlockPosition current = queue.poll();

            Block footBlock = world.getBlockAt(current.x, current.y, current.z);
            if (!footBlock.getType().isSolid()) continue;

            Location playerLoc = new Location(
                    world,
                    current.x + 0.5,
                    current.y + 1,
                    current.z + 0.5,
                    yaw,
                    pitch
            );

            if (isSafeLocation(playerLoc)) {
                return playerLoc;
            }

            addNeighbors(current, queue, visited, world);
        }

        // 如果找不到安全位置，返回原始位置但保留朝向
        return new Location(world, originalLocation.getX(), originalLocation.getY(), originalLocation.getZ(), yaw, pitch);
    }

    private static boolean isSafeLocation(Location location) {
        Block footBlock = location.clone().subtract(0, 1, 0).getBlock();
        if (!footBlock.getType().isSolid()) return false;

        Block currentBlock = location.getBlock();
        Material currentType = currentBlock.getType();
        if (currentType == Material.WATER || currentType == Material.LAVA || currentBlock.getType().isSolid()) {
            return false;
        }

        Block headBlock = location.clone().add(0, 1, 0).getBlock();
        Material headType = headBlock.getType();
        return headType != Material.WATER && headType != Material.LAVA && !headBlock.getType().isSolid();
    }

    private static void addNeighbors(BlockPosition pos, PriorityQueue<BlockPosition> queue, Set<BlockPosition> visited, World world) {
        int[][] directions = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
        for (int[] dir : directions) {
            int nx = pos.x + dir[0];
            int ny = pos.y + dir[1];
            int nz = pos.z + dir[2];

            if (ny < world.getMinHeight() || ny >= world.getMaxHeight()) continue;

            BlockPosition neighbor = new BlockPosition(nx, ny, nz);
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
    }

    private static double distanceSquared(BlockPosition blockPos, Location original) {
        double px = blockPos.x + 0.5;
        double py = blockPos.y + 1;
        double pz = blockPos.z + 0.5;
        return Math.pow(px - original.getX(), 2) + Math.pow(py - original.getY(), 2) + Math.pow(pz - original.getZ(), 2);
    }

    private static class BlockPosition {
        public final int x;
        public final int y;
        public final int z;

        public BlockPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            BlockPosition that = (BlockPosition) obj;
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, y, z);
        }
    }
}