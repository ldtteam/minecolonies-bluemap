package com.ldtteam.minecoloniesbluemap.integration;

import com.ldtteam.minecoloniesbluemap.Constants;
import com.ldtteam.minecoloniesbluemap.integration.markers.ColonyMarker;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
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
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BluemapIntegration
{
    /**
     * The market set identifiers.
     */
    public static final ResourceLocation COLONIES_MARKER_SET  = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "colonies");
    public static final ResourceLocation BUILDINGS_MARKER_SET = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "buildings");
    public static final ResourceLocation CITIZENS_MARKER_SET  = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "citizens");

    /**
     * The market set display names.
     */
    private static final String COLONIES_MARKER_SET_NAME  = "MineColonies: Colonies";
    private static final String BUILDINGS_MARKER_SET_NAME = "MineColonies: Buildings";
    private static final String CITIZENS_MARKER_SET_NAME  = "MineColonies: Citizens";

    private static final int COLONY_MARKER_MAX_DISTANCE = 5000;

    /**
     * The BlueMap API instance.
     */
    @Nullable
    private BlueMapAPI api;

    /**
     * Triggered upon enabling of the BlueMap API.
     *
     * @param api the BlueMap API instance.
     */
    public void onEnable(final BlueMapAPI api)
    {
        this.api = api;
        BlueMapAssetManager.createAssets(api);

        api.getMaps().forEach(bMap -> {
            bMap.getMarkerSets().computeIfAbsent(COLONIES_MARKER_SET.toString(), k -> MarkerSet.builder().label(COLONIES_MARKER_SET_NAME).toggleable(true).build());
            bMap.getMarkerSets()
                .computeIfAbsent(BUILDINGS_MARKER_SET.toString(), k -> MarkerSet.builder().label(BUILDINGS_MARKER_SET_NAME).toggleable(true).defaultHidden(true).build());
            bMap.getMarkerSets()
                .computeIfAbsent(CITIZENS_MARKER_SET.toString(), k -> MarkerSet.builder().label(CITIZENS_MARKER_SET_NAME).toggleable(true).defaultHidden(true).build());
        });

        IMinecoloniesAPI.getInstance().getColonyManager().getAllColonies().forEach(colony -> {
            createColonyMarker(colony);
        });
    }

    /**
     * Internal common method for creating a fully new colony marker.
     *
     * @param colony the target colony.
     */
    private void createColonyMarker(final IColony colony)
    {
        final ExtrudeMarker marker = ExtrudeMarker.builder()
            .label(colony.getName())
            .detail(ColonyMarker.createDetails(colony))
            .shape(ColonyMarker.createShape(colony), colony.getCenter().getY() - ChunkPos.REGION_SIZE * 2F, colony.getCenter().getY() + ChunkPos.REGION_SIZE * 2F)
            .lineColor(ColonyMarker.getLineColor(colony))
            .fillColor(ColonyMarker.getFillColor(colony))
            .maxDistance(COLONY_MARKER_MAX_DISTANCE)
            .build();

        executeForEachMap(colony, bMap -> bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).put(String.valueOf(colony.getID()), marker));
    }

    /**
     * Internal common method for ensuring an action is executed for each map, assuming the BlueMap API is loaded.
     *
     * @param colony the target colony.
     * @param action the action to perform for each map.
     */
    private void executeForEachMap(final IColony colony, final Consumer<BlueMapMap> action)
    {
        if (this.api == null)
        {
            return;
        }

        api.getWorld(colony.getWorld()).ifPresent(bWorld -> bWorld.getMaps().forEach(action));
    }

    /**
     * Triggered upon disabling of the BlueMap API.
     *
     * @param api the BlueMap API instance.
     */
    public void onDisable(final BlueMapAPI api)
    {
        api.getMaps().forEach(bMap -> {
            bMap.getMarkerSets().remove(COLONIES_MARKER_SET.toString());
            bMap.getMarkerSets().remove(BUILDINGS_MARKER_SET.toString());
            bMap.getMarkerSets().remove(CITIZENS_MARKER_SET.toString());
        });
        this.api = null;
    }

    /**
     * Event handler for {@link ColonyCreatedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onColonyCreated(@NotNull final ColonyCreatedModEvent event)
    {
        createColonyMarker(event.getColony());
    }

    /**
     * Event handler for {@link ColonyDeletedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onColonyDeleted(@NotNull final ColonyDeletedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).remove(String.valueOf(event.getColony().getID())));
    }

    /**
     * Event handler for {@link ColonyNameChangedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onColonyNameChanged(@NotNull final ColonyNameChangedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setLabel(event.getColony().getName());
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }

    /**
     * Event handler for {@link ColonyTeamColorChangedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onColonyTeamColorChanged(@NotNull final ColonyTeamColorChangedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setLineColor(ColonyMarker.getLineColor(event.getColony()));
            marker.setFillColor(ColonyMarker.getFillColor(event.getColony()));
        });
    }

    /**
     * Event handler for {@link BuildingAddedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onBuildingAdded(@NotNull BuildingAddedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }

    /**
     * Event handler for {@link BuildingConstructionModEvent} events.
     *
     * @param event the event instance.
     */
    public void onBuildingConstruction(@NotNull final BuildingConstructionModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }

    /**
     * Event handler for {@link BuildingRemovedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onBuildingRemoved(@NotNull BuildingRemovedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }

    /**
     * Event handler for {@link CitizenAddedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onCitizenAdded(@NotNull final CitizenAddedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }

    /**
     * Event handler for {@link CitizenJobChangedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onCitizenJobChanged(@NotNull CitizenJobChangedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }

    /**
     * Event handler for {@link CitizenDiedModEvent} events.
     *
     * @param event the event instance.
     */
    public void onCitizenDied(@NotNull final CitizenDiedModEvent event)
    {
        executeForEachMap(event.getColony(), bMap -> {
            final ExtrudeMarker marker = (ExtrudeMarker) bMap.getMarkerSets().get(COLONIES_MARKER_SET.toString()).get(String.valueOf(event.getColony().getID()));
            marker.setDetail(ColonyMarker.createDetails(event.getColony()));
        });
    }
}
