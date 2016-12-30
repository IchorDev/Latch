/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) IchorPowered <https://github.com/IchorPowered>
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

package com.meronat.latch.listeners;

import com.meronat.latch.Latch;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Direction;

public class NotifyNeighborListener {

    /* Does not seem to always return true even if the block is powered, look to re-implement this in the future.

    //Cover all the ways the block could be powered
    private boolean isPowered(Location<?> location) {
        return (location.get(Keys.POWER).isPresent() && location.get(Keys.POWER).get() > 0) ||
                (location.getProperty(PoweredProperty.class).isPresent() && location.getProperty(PoweredProperty.class).get().getValue()) ||
                (location.getProperty(IndirectlyPoweredProperty.class).isPresent() && location.getProperty(IndirectlyPoweredProperty.class).get().getValue());
    }

    */

    @Listener
    public void notifyNeighbors(NotifyNeighborBlockEvent event, @First BlockSnapshot cause) {
        cause.getLocation().ifPresent(worldLocation -> {
            for (Direction d : event.getOriginalNeighbors().keySet()) {
                Latch.getLockManager().getLock(worldLocation.getBlockRelative(d)).ifPresent(lock -> {
                    if (lock.getProtectFromRedstone()) {
                        event.getNeighbors().remove(d);
                    }
                });

            }

        });

    }

}
