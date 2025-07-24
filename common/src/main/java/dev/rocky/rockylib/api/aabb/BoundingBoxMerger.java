package dev.rocky.rockylib.api.aabb;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @deprecated This helper will be replaced by a versioned alternative in a future release.
 */
@Deprecated
public final class BoundingBoxMerger {
    private static final Long2ObjectMap<Direction> DIRECTION_LOOKUP = Arrays.stream(Direction.values())
            .collect(Collectors.toMap(dir -> new BlockPos(dir.getUnitVec3i()).asLong(),
                    dir -> dir,
                    (a, b) -> {
                        throw new IllegalStateException("Duplicate direction detected.");
                    },
                    Long2ObjectOpenHashMap::new
            ));
    private final Map<Vec3, AABB> positionToBox = new HashMap<>();
    private final Multimap<AABB, Vec3> boxToPosition = HashMultimap.create();
    private double xCoordTracker = Double.NEGATIVE_INFINITY;
    private double yCoordTracker = Double.NEGATIVE_INFINITY;
    private Vec3 currentCenter = null;
    private AABB currentBounds = null;

    /**
     * Merges the provided block positions into a collection of bounding boxes
     * by attempting to combine adjacent positions into larger axis-aligned bounding boxes (AABBs).
     *
     * @param positions      The block positions to merge.
     * @param referencePoint The reference point for normalization.
     * @return A collection of merged bounding boxes.
     */
    public static Collection<AABB> merge(Collection<BlockPos> positions, BlockPos referencePoint) {
        BoundingBoxMerger boxMerger = new BoundingBoxMerger();

        positions.stream()
                .map(pos -> pos.subtract(referencePoint))
                .sorted()
                .map(AABB::new)
                .forEachOrdered(box -> {
                    // Reset current bounds if we encounter a new x or y coordinate
                    if (boxMerger.xCoordTracker != box.minX || boxMerger.yCoordTracker != box.minY) {
                        boxMerger.currentBounds = null;
                    }

                    boxMerger.xCoordTracker = box.minX;
                    boxMerger.yCoordTracker = box.minY;

                    Vec3 center = box.getCenter();
                    boxMerger.currentCenter = center;

                    // Attempt to combine with the current bounds or adjacent boxes
                    if (boxMerger.currentBounds != null && boxMerger.canCombine(
                            boxMerger.currentBounds,
                            box,
                            center
                    )) {
                        return;
                    }

                    if (boxMerger.tryCombineAdjacent(center, box)) {
                        return;
                    }

                    // Store as a new bounding box
                    boxMerger.currentBounds = box;
                    boxMerger.positionToBox.put(center, box);
                    boxMerger.boxToPosition.put(box, center);
                });

        return boxMerger.boxToPosition.keySet();
    }

    /**
     * Determines if two bounding boxes are aligned along a given direction.
     *
     * @param first     The first bounding box.
     * @param second    The second bounding box.
     * @param direction The direction to check for alignment.
     * @return True if the bounding boxes are aligned, false otherwise.
     */
    private static boolean isAligned(AABB first, AABB second, Direction direction) {
        return getAxisValue(first, direction) == getAxisValue(
                second,
                direction.getOpposite()
        ) && Arrays.stream(Direction.values())
                .filter(d -> d.getAxis() != direction.getAxis())
                .allMatch(d -> getAxisValue(first, d) == getAxisValue(second, d));
    }

    /**
     * Retrieves the value of a bounding box along a specified direction.
     *
     * @param box       The bounding box.
     * @param direction The direction to evaluate.
     * @return The corresponding value along the given direction.
     */
    private static double getAxisValue(AABB box, Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                box.max(direction.getAxis()) : box.min(
                direction.getAxis());
    }

    /**
     * Converts a vector into a direction based on its coordinates.
     *
     * @param vector The vector to convert.
     * @return The corresponding direction, or null if none exists.
     */
    private static Direction directionFromVector(Vec3 vector) {
        return DIRECTION_LOOKUP.get(BlockPos.asLong((int) vector.x, (int) vector.y, (int) vector.z));
    }

    /**
     * Attempts to merge the current bounding box with its neighboring bounding box.
     *
     * @param current  The current bounding box.
     * @param neighbor The neighboring bounding box.
     * @param center   The center position of the neighboring box.
     * @return True if the boxes are neighbors and merged, false otherwise.
     */
    private boolean canCombine(AABB current, AABB neighbor, Vec3 center) {
        Direction direction = directionFromVector(center.subtract(current.getCenter()));
        return direction != null && isAligned(current, neighbor, direction) && mergeBoxes(
                current,
                neighbor,
                center
        );
    }

    /**
     * Attempts to merge the provided bounding box with any neighboring bounding boxes.
     *
     * @param center The center position of the current bounding box.
     * @param box    The bounding box to merge with.
     * @return True if a neighbor is found and merged, false otherwise.
     */
    private boolean tryCombineAdjacent(Vec3 center, AABB box) {
        for (Direction direction : Direction.values()) {
            Vec3 adjacentCenter = center.add(Vec3.atLowerCornerOf(direction.getUnitVec3i()));
            AABB adjacentBox = positionToBox.get(adjacentCenter);

            if (adjacentBox != null && isAligned(box, adjacentBox, direction)) {
                return mergeBoxes(adjacentBox, box, center);
            }
        }
        return false;
    }

    /**
     * Merges two bounding boxes and updates the necessary mappings.
     *
     * @param source The initial bounding box.
     * @param target The bounding box to merge with.
     * @param center The center position of the target bounding box.
     * @return True once the merge is successful.
     */
    private boolean mergeBoxes(AABB source, AABB target, Vec3 center) {
        AABB expanded = source.minmax(target);

        Set<Vec3> mergedPositions = new HashSet<>(boxToPosition.removeAll(source));
        mergedPositions.forEach(v -> positionToBox.put(v, expanded));

        boxToPosition.putAll(expanded, mergedPositions);
        positionToBox.put(center, expanded);
        boxToPosition.put(expanded, center);

        currentBounds = expanded;
        return true;
    }
}
