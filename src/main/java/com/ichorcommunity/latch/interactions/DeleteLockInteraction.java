package com.ichorcommunity.latch.interactions;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.utils.LatchUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class DeleteLockInteraction implements AbstractLockInteraction {

    private final UUID player;

    private boolean persisting = false;

    public DeleteLockInteraction(UUID player) {
        this.player = player;
    }

    @Override
    public boolean handleInteraction(Player player, Location<World> location, BlockSnapshot blockstate) {
        Optional<Lock> lock = Latch.lockManager.getLock(location);
        //Check to see if another lock is present
        if(!lock.isPresent()) {
            player.sendMessage(Text.of("There is no lock there."));
            return false;
        }

        //Check to make sure they're the owner
        if(!lock.get().isOwner(player.getUniqueId())) {
            player.sendMessage(Text.of("You're not the owner of this lock."));
            return false;
        }

        Optional<Location<World>> optionalOtherBlock = LatchUtils.getDoubleBlockLocation(blockstate);
        Optional<Lock> otherBlockLock = Optional.ofNullable(null);

        //If the block has another block that needs to be unlocked
        if(optionalOtherBlock.isPresent()) {
            otherBlockLock = Optional.ofNullable(Latch.lockManager.getLock(optionalOtherBlock.get()).get());
        }

        player.sendMessage(Text.of("You have deleted this " + lock.get().getLockedObject() + " lock."));
        Latch.lockManager.deleteLock(location);

        if(otherBlockLock.isPresent()) {
            player.sendMessage(Text.of("You have also deleted the adjacent " + otherBlockLock.get().getLockedObject() + " lock."));
            Latch.lockManager.deleteLock(otherBlockLock.get().getLocation());
        }
        return true;
    }

    @Override
    public boolean shouldPersist() {
        return persisting;
    }

    @Override
    public void setPersistance(boolean persist) {
        this.persisting = persist;
    }
}
