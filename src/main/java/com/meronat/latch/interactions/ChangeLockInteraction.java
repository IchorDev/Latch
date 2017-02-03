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

package com.meronat.latch.interactions;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.enums.LockType;
import com.meronat.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class ChangeLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private LockType type;
    private String password;
    private String lockName;
    private UUID newOwner;
    private Collection<UUID> membersToAdd;
    private Collection<UUID> membersToRemove;

    private boolean persisting = false;
    private Boolean protectFromRedstone;


    public ChangeLockInteraction(UUID player) {
        this.player = player;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public void setNewOwner(UUID newOwner) {
        this.newOwner = newOwner;
    }

    public void setMembersToAdd(Collection<UUID> members) {
        this.membersToAdd = members;
    }

    public void setMembersToRemove(Collection<UUID> members) {
        this.membersToRemove = members;
    }

    public void setProtectFromRedstone(Boolean protectFromRedstone) { this.protectFromRedstone = protectFromRedstone; }



    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockState) {
        Optional<Lock> optionalLock = Latch.getLockManager().getLock(location);
        //Check to see if another lock is present
        if(!optionalLock.isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "There is no lock there."));
            return false;
        }

        Lock lock = optionalLock.get();

        //Check to make sure they're the owner
        if(!lock.isOwnerOrBypassing(player.getUniqueId())) {
            player.sendMessage(Text.of(TextColors.RED, "You are not the owner of this lock."));
            return false;
        }

        //Check to make sure, if they're assigning a new owner, the new owner is not at their limit
        if(newOwner != null || type != null) {
            if (Latch.getLockManager().isPlayerAtLockLimit(newOwner == null ? lock.getOwner() : newOwner, type == null ? lock.getLockType() : type)) {
                player.sendMessage(Text.of(TextColors.RED, "You cannot change this lock due to lock limits."));
                return false;
            }
        }

        UUID originalOwner = lock.getOwner();
        String originalName = lock.getName();

        if(type != null) {
            lock.setType(type);
        }
        if(password != null) {
            lock.setSalt(LatchUtils.generateSalt());
            lock.changePassword(LatchUtils.hashPassword(password, lock.getSalt()));

            //If changing password, need to clear out ability to access
            Latch.getLockManager().removeAllLockAccess(lock);
        }
        if(lockName != null) {
            lock.setName(lockName);
        }
        if(newOwner != null) {
            //If assigning to a new owner - need to validate the name
            if(!Latch.getLockManager().isUniqueName(newOwner, lock.getName())) {
                lock.setName(LatchUtils.getRandomLockName(newOwner, lock.getLockedObject()));
            }
            lock.setOwner(newOwner);
        }
        if(membersToAdd != null) {
            for(UUID u : membersToAdd) {
                Latch.getLockManager().addLockAccess(lock, u);
            }
        }
        if(membersToRemove != null) {
            for(UUID u : membersToRemove) {
                Latch.getLockManager().removeLockAccess(lock, u);
            }
        }
        if(protectFromRedstone != null) {
            lock.setProtectFromRedstone(protectFromRedstone);
        }

        //Update the base lock elements
        if(lockName != null || type != null || password != null || newOwner != null || protectFromRedstone != null) {
            Latch.getLockManager().updateLockAttributes(originalOwner, originalName, lock);
        }

        player.sendMessage(Text.of(TextColors.DARK_GREEN, "Lock data has been successfully updated."));

        return true;

    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistence(boolean persist) {
        this.persisting = persist;
    }

}
