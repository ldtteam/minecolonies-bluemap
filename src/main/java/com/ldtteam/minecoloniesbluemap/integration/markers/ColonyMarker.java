package com.ldtteam.minecoloniesbluemap.integration.markers;

import com.ldtteam.minecoloniesbluemap.Constants;
import com.ldtteam.minecoloniesbluemap.area.AreaGenerator;
import com.ldtteam.minecoloniesbluemap.area.ColonyArea;
import com.ldtteam.minecoloniesbluemap.area.ColonyChunkClaimCalculator;
import com.ldtteam.minecoloniesbluemap.util.HtmlParser;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.CitizenConstants;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;

public class ColonyMarker
{
    /**
     * Template identifiers.
     */
    private static final ResourceLocation TEMPLATE_MARKER_COLONY                        = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "markers/colony.html");
    private static final ResourceLocation TEMPLATE_MARKER_COLONY_BUILDING_ROW           = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "markers/building_row.html");
    private static final ResourceLocation TEMPLATE_MARKER_COLONY_CITIZEN_EMPLOYED_ROW   =
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "markers/citizen_employed_row.html");
    private static final ResourceLocation TEMPLATE_MARKER_COLONY_CITIZEN_UNEMPLOYED_ROW =
        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "markers/citizen_unemployed_row.html");

    private static final float COLONY_MARKER_LINE_COLOR_ALPHA = 1F;
    private static final float COLONY_MARKER_FILL_COLOR_ALPHA = 0.3F;

    private ColonyMarker() {}

    public static String createDetails(final IColony colony)
    {
        final Map<String, String> markerContext = new HashMap<>();
        markerContext.put("icon", getColonyIcon(colony));
        markerContext.put("colony", colony.getName());
        markerContext.put("mayor", colony.getPermissions().getOwnerName());
        markerContext.put("style", colony.getStructurePack());
        markerContext.put("building_count", String.valueOf(colony.getBuildingManager().getBuildings().size()));
        markerContext.put("citizen_count", String.valueOf(colony.getCitizenManager().getCitizens().size()));

        final Map<BuildingEntry, Long> buildingsData =
            colony.getBuildingManager().getBuildings().values().stream().collect(Collectors.groupingBy(IBuilding::getBuildingType, Collectors.counting()));

        final List<String> buildingInfo = new ArrayList<>();
        for (final Map.Entry<BuildingEntry, Long> building : buildingsData.entrySet())
        {
            final Map<String, String> buildingMarkerContext = new HashMap<>();
            buildingMarkerContext.put("building_type", I18n.get(building.getKey().getTranslationKey()));
            buildingMarkerContext.put("count", String.valueOf(building.getValue()));
            buildingInfo.add(HtmlParser.parseHtmlFile(TEMPLATE_MARKER_COLONY_BUILDING_ROW, buildingMarkerContext));
        }
        markerContext.put("building_info", String.join("", buildingInfo));

        final List<String> citizenInfo = new ArrayList<>();
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            final Map<String, String> citizenMarkerContext = new HashMap<>();
            citizenMarkerContext.put("name", citizen.getName());
            final Optional<String> jobText = Optional.ofNullable(citizen.getJob()).map(IJob::getJobRegistryEntry).map(JobEntry::getTranslationKey).map(I18n::get);
            if (jobText.isPresent())
            {
                citizenMarkerContext.put("job", jobText.get());
                citizenInfo.add(HtmlParser.parseHtmlFile(TEMPLATE_MARKER_COLONY_CITIZEN_EMPLOYED_ROW, citizenMarkerContext));
            }
            else
            {
                citizenInfo.add(HtmlParser.parseHtmlFile(TEMPLATE_MARKER_COLONY_CITIZEN_UNEMPLOYED_ROW, citizenMarkerContext));
            }
        }
        markerContext.put("citizen_info", String.join("", citizenInfo));

        return HtmlParser.parseHtmlFile(TEMPLATE_MARKER_COLONY, markerContext);
    }

    private static String getColonyIcon(final IColony colony)
    {
        int currentCitizens = colony.getCitizenManager().getCitizens().size();
        String icon = "outpost";
        if (currentCitizens >= CitizenConstants.CITIZEN_LIMIT_VILLAGE)
        {
            icon = "city";
        }
        else if (currentCitizens >= CitizenConstants.CITIZEN_LIMIT_HAMLET)
        {
            icon = "village";
        }
        else if (currentCitizens >= CitizenConstants.CITIZEN_LIMIT_OUTPOST)
        {
            icon = "hamlet";
        }

        return icon;
    }

    public static Shape createShape(final IColony colony)
    {
        final Collection<ChunkPos> claimedChunks = ColonyChunkClaimCalculator.getAllClaimedChunks(colony);
        final ColonyArea area = AreaGenerator.generateAreaFromChunks(claimedChunks);
        return Shape.builder().addPoints(area.getPoints()).build();
    }

    public static Color getLineColor(final IColony colony)
    {
        final Color baseColor = new Color(Optional.ofNullable(colony.getTeamColonyColor()).map(ChatFormatting::getColor).orElse(0));
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), COLONY_MARKER_LINE_COLOR_ALPHA);
    }

    public static Color getFillColor(final IColony colony)
    {
        final Color baseColor = new Color(Optional.ofNullable(colony.getTeamColonyColor()).map(ChatFormatting::getColor).orElse(0));
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), COLONY_MARKER_FILL_COLOR_ALPHA);
    }
}
