package com.ldtteam.minecoloniesbluemap;

import com.ldtteam.minecoloniesbluemap.integration.BluemapIntegration;
import com.ldtteam.minecoloniesbluemap.util.Log;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.eventbus.events.colony.ColonyCreatedModEvent;
import com.minecolonies.api.eventbus.events.colony.ColonyDeletedModEvent;
import com.minecolonies.api.eventbus.events.colony.ColonyNameChangedModEvent;
import com.minecolonies.api.eventbus.events.colony.ColonyTeamColorChangedModEvent;
import com.minecolonies.api.eventbus.events.colony.buildings.BuildingAddedModEvent;
import com.minecolonies.api.eventbus.events.colony.buildings.BuildingConstructionModEvent;
import com.minecolonies.api.eventbus.events.colony.buildings.BuildingRemovedModEvent;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenAddedModEvent;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenDiedModEvent;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenJobChangedModEvent;
import de.bluecolored.bluemap.api.BlueMapAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;

@Mod(Constants.MOD_ID)
public class MinecoloniesBluemap
{
    private final BluemapIntegration integration;

    public MinecoloniesBluemap(final IEventBus modBus)
    {
        Log.getLogger().info("Loading MinecoloniesBluemap...");
        this.integration = new BluemapIntegration();

        BlueMapAPI.onEnable(this.integration::onEnable);
        BlueMapAPI.onDisable(this.integration::onDisable);

        modBus.addListener(this::preInit);
    }

    /**
     * Event handler for forge pre init event.
     *
     * @param event the forge pre init event.
     */
    public void preInit(@NotNull final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(ColonyCreatedModEvent.class, integration::onColonyCreated);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(ColonyDeletedModEvent.class, integration::onColonyDeleted);

            IMinecoloniesAPI.getInstance().getEventBus().subscribe(ColonyNameChangedModEvent.class, integration::onColonyNameChanged);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(ColonyTeamColorChangedModEvent.class, integration::onColonyTeamColorChanged);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(BuildingAddedModEvent.class, integration::onBuildingAdded);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(BuildingConstructionModEvent.class, integration::onBuildingConstruction);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(BuildingRemovedModEvent.class, integration::onBuildingRemoved);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(CitizenAddedModEvent.class, integration::onCitizenAdded);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(CitizenJobChangedModEvent.class, integration::onCitizenJobChanged);
            IMinecoloniesAPI.getInstance().getEventBus().subscribe(CitizenDiedModEvent.class, integration::onCitizenDied);
        });
    }
}
