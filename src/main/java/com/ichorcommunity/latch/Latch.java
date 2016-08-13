/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ichor Community <http://www.ichorcommunity.com>
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

package com.ichorcommunity.latch;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import com.ichorcommunity.latch.commands.BaseCommand;
import com.ichorcommunity.latch.commands.UnlockCommand;
import com.ichorcommunity.latch.entities.LockManager;
import com.ichorcommunity.latch.listeners.ChangeBlockListener;
import com.ichorcommunity.latch.listeners.InteractBlockListener;
import com.ichorcommunity.latch.listeners.SpawnEntityListener;
import com.ichorcommunity.latch.storage.SqlHandler;

import net.minecrell.mcstats.SpongeStatsLite;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(
        id = "latch",
        name = "Latch",
        version = "0.0.3",
        description = "A locking plugin which optionally allows you to lockpick those locks.",
        url = "http://ichorcommunity.com/",
        authors = {
                "Nighteyes604",
                "Meronat"
        }
)
public class Latch {

    private static Logger logger;
    private static Path configPath;

    private static LockManager lockManager = new LockManager();

    private static SqlHandler storageHandler;

    @Inject
    public SpongeStatsLite stats;

    @Inject
    public Latch(Logger logger, @DefaultConfig(sharedRoot = false) Path configPath) {
        Latch.logger = logger;
        Latch.configPath = configPath;

        storageHandler = new SqlHandler();
    }

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private static Configuration config;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        this.stats.start();
    }


    @Listener
    public void onGameInit(GameInitializationEvent event) {
        config = new Configuration(configManager);
        loadConfigurationData();

        registerListeners();

        Sponge.getCommandManager().register(this, new BaseCommand().baseCommand, "latch", "lock");
        Sponge.getCommandManager().register(this, new UnlockCommand().getCommand(), "unlock", "unlatch");

        //Register permissions
        if(getConfig().getNode("add_default_permissions").getBoolean()) {
            Optional<PermissionService> ps = Sponge.getServiceManager().provide(PermissionService.class);
            if (ps.isPresent()) {
                ps.get().getUserSubjects().getDefaults().getSubjectData().setPermission(ps.get().getDefaults().getActiveContexts(), "latch.normal", Tristate.TRUE);
            }
        }


    }

    public static Logger getLogger() {
        return logger;
    }

    private void registerListeners() {
        Sponge.getEventManager().registerListeners(this, new ChangeBlockListener());
        Sponge.getEventManager().registerListeners(this, new InteractBlockListener());
        Sponge.getEventManager().registerListeners(this, new SpawnEntityListener());
    }

    private void loadConfigurationData() {
        List<String> configBlockNames = new ArrayList<>();
        List<String> restrictedBlockNames = new ArrayList<>();
        List<String> protectBelowBlocks = new ArrayList<>();
        HashMap<String, Integer> lockLimits = new HashMap<>();

        try {
            configBlockNames = getConfig().getNode("lockable_blocks").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of lockable_blocks.");
            e.printStackTrace();
        }

        try {
            restrictedBlockNames = getConfig().getNode("prevent_adjacent_to_locks").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of prevent_adjacent_to_locks.");
            e.printStackTrace();
        }

        try {
            protectBelowBlocks = getConfig().getNode("protect_below_block").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            getLogger().error("Error loading list of protect_below_block.");
            e.printStackTrace();
        }

        try {
            lockLimits = (HashMap<String, Integer>) getConfig().getNode("lock_limit").getValue(lockLimits);
        } catch (Exception e) {
            getLogger().error("Error loading lock limits");
            e.printStackTrace();
        }

        lockManager.setLockableBlocks(configBlockNames);
        lockManager.setRestrictedBlocks(restrictedBlockNames);
        lockManager.setProtectBelowBlocks(protectBelowBlocks);
        lockManager.setLockLimits(lockLimits);
    }

    public static SqlHandler getStorageHandler() { return storageHandler;}

    public static LockManager getLockManager() { return lockManager;}

    public static CommentedConfigurationNode getConfig() {
        return config.getConfigurationNode();
    }

    public static Path getConfigPatch() { return configPath; }
}