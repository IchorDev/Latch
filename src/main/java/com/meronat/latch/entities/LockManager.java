/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.meronat.latch.entities;

import com.google.common.collect.ImmutableMap;
import com.meronat.latch.Latch;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.interactions.LockInteraction;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class LockManager {

    private HashMap<String, Integer> lockLimits = new HashMap<>();
    private HashMap<UUID, LockInteraction> interactionData = new HashMap<>();

    private Set<UUID> bypassing = new HashSet<>();

    private List<String> lockableBlocks = new ArrayList<>();
    private List<String> restrictedBlocks = new ArrayList<>();
    private List<String> protectBelowBlocks = new ArrayList<>();

    private boolean protectFromRedstone = false;

    public Optional<Lock> getLock(Location location) {
        return Latch.getStorageHandler().getLockByLocation(location);
    }

    public void createLock(Lock lock) {
        Latch.getStorageHandler().createLock(lock, lock.getLocations(), lock.getAccessors());
    }

    public void deleteLock(Location<World> location, boolean deleteEntireLock) {
        Latch.getStorageHandler().deleteLock(location, deleteEntireLock);
    }

    /*
     * Locks that someone should be able to enter a password and access (or gain perm access to)
     */
    public boolean isPasswordCompatibleLock(Lock lock) {
        //If the lock is one of the two password locks
        return lock.getLockType() == LockType.PASSWORD_ALWAYS || lock.getLockType() == LockType.PASSWORD_ONCE;
    }

    public boolean hasInteractionData(UUID uniqueId) {
        return this.interactionData.containsKey(uniqueId);
    }

    public LockInteraction getInteractionData(UUID uniqueId) {
        return this.interactionData.get(uniqueId);
    }

    public void setInteractionData(UUID uniqueId, LockInteraction lockInteraction) {
        this.interactionData.put(uniqueId, lockInteraction);
    }

    public void setLockableBlocks(List<String> lockableBlocks) {
        this.lockableBlocks = lockableBlocks;
    }

    public void setRestrictedBlocks(List<String> preventAdjacentToLocks) {
        this.restrictedBlocks = preventAdjacentToLocks;
    }

    public void setProtectBelowBlocks(List<String> protectBelowBlocks) {
        this.protectBelowBlocks = protectBelowBlocks;
    }

    public boolean isRestrictedBlock(BlockType type) {
        return this.restrictedBlocks.contains(type.getId());
    }

    public boolean isLockableBlock(BlockType block) {
        return this.lockableBlocks.contains(block.getId());
    }

    public boolean isProtectBelowBlocks(BlockType block) {
        return this.protectBelowBlocks.contains(block.getId());
    }

    public void removeInteractionData(UUID uniqueId) {
        this.interactionData.remove(uniqueId);
    }

    public void addLockAccess(Lock thisLock, UUID uniqueId) {
        if (!thisLock.canAccess(uniqueId)) {
            thisLock.addAccess(uniqueId);
            Latch.getStorageHandler().addLockAccess(thisLock, uniqueId);
        }
    }

    public boolean isUniqueName(UUID playerUUID, String lockName) {
        return Latch.getStorageHandler().isUniqueName(playerUUID, lockName);
    }

    public void addLockLocation(Lock lock, Location<World> location) {
        if (!lock.getLocations().contains(location)) {
            Latch.getStorageHandler().addLockLocation(lock, location);
        }
    }

    public void removeAllLockAccess(Lock thisLock) {
        thisLock.getAccessors().clear();
        Latch.getStorageHandler().removeAllLockAccess(thisLock);
    }

    public void removeLockAccess(Lock thisLock, UUID uniqueId) {
        if (thisLock.canAccess(uniqueId)) {
            thisLock.removeAccess(uniqueId);
            Latch.getStorageHandler().removeLockAccess(thisLock, uniqueId);

        }
    }

    public void updateLockAttributes(UUID originalOwner, String originalName, Lock lock) {
        Latch.getStorageHandler().updateLockAttributes(originalOwner, originalName, lock);
    }

    public List<Lock> getPlayersLocks(UUID uniqueId) {
        return Latch.getStorageHandler().getLocksByOwner(uniqueId);
    }

    public void setLockLimits(HashMap<String, Integer> lockLimits) {
        this.lockLimits.clear();
        for (Map.Entry<String, Integer> limit : lockLimits.entrySet()) {
            //Only add if limit >=0, otherwise no limit
            if (limit.getValue() >= 0) {
                this.lockLimits.put(limit.getKey().toLowerCase(), limit.getValue());
            }
        }
    }

    public boolean isPlayerAtLockLimit(UUID player, LockType type) {
        return Latch.getStorageHandler().isPlayerAtLockLimit(player, type, this.lockLimits);
    }

    public ImmutableMap<String, Integer> getLimits() {
        return ImmutableMap.copyOf(this.lockLimits);
    }

    public boolean getProtectFromRedstone() {
        return this.protectFromRedstone;
    }

    public void setProtectFromRedstone(boolean protectFromRedstone) {
        this.protectFromRedstone = protectFromRedstone;
    }

    public boolean isBypassing(UUID uuid) {
        return this.bypassing.contains(uuid);
    }

    public void setBypassing(UUID uuid) {
        this.bypassing.add(uuid);
    }

    public void removeBypassing(UUID uuid) {
        this.bypassing.remove(uuid);
    }

}
