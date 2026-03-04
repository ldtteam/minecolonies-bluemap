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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Reusable methods for determining colony marker info.
 */
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

    /**
     * Alpha color for the line color.
     */
    private static final float COLONY_MARKER_LINE_COLOR_ALPHA = 1F;

    /**
     * Alpha color for the fill color.
     */
    private static final float COLONY_MARKER_FILL_COLOR_ALPHA = 0.3F;

    private ColonyMarker() {}

    /**
     * Creates the HTML details string for the colony marker.
     *
     * @param colony the target colony.
     * @return the HTML string.
     */
    public static String createDetails(final IColony colony)
    {
        final Map<BuildingEntry, Long> buildingsData =
            colony.getServerBuildingManager().getBuildings().values().stream().collect(Collectors.groupingBy(IBuilding::getBuildingType, Collectors.counting()));

        final List<HtmlParser.HttpContent> buildingInfo = new ArrayList<>();
        for (final Map.Entry<BuildingEntry, Long> building : buildingsData.entrySet())
        {
            final HtmlParser.HttpParsingContext buildingMarkerContext = HtmlParser.HttpParsingContext.builder()
                .withVariable("building_type", Component.translatable(building.getKey().getTranslationKey()).getString())
                .withVariable("count", String.valueOf(building.getValue()));
            buildingInfo.add(HtmlParser.parseHtml(TEMPLATE_MARKER_COLONY_BUILDING_ROW, buildingMarkerContext));
        }

        final List<HtmlParser.HttpContent> citizenInfo = new ArrayList<>();
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            final HtmlParser.HttpParsingContext citizenMarkerContext = HtmlParser.HttpParsingContext.builder().withVariable("name", citizen.getName());

            final Optional<String> jobText =
                Optional.ofNullable(citizen.getJob()).map(IJob::getJobRegistryEntry).map(JobEntry::getTranslationKey).map(Component::translatable).map(MutableComponent::getString);
            if (jobText.isPresent())
            {
                citizenMarkerContext.withVariable("job", jobText.get());
                citizenInfo.add(HtmlParser.parseHtml(TEMPLATE_MARKER_COLONY_CITIZEN_EMPLOYED_ROW, citizenMarkerContext));
            }
            else
            {
                citizenInfo.add(HtmlParser.parseHtml(TEMPLATE_MARKER_COLONY_CITIZEN_UNEMPLOYED_ROW, citizenMarkerContext));
            }
        }

        final HtmlParser.HttpParsingContext markerContext = HtmlParser.HttpParsingContext.builder()
            .withVariable("icon", getColonyIcon(colony))
            .withVariable("colony", colony.getName())
            .withVariable("mayor", colony.getPermissions().getOwnerName())
            .withVariable("style", colony.getStructurePack())
            .withVariable("building_count", String.valueOf(colony.getServerBuildingManager().getBuildings().size()))
            .withVariable("citizen_count", String.valueOf(colony.getCitizenManager().getCitizens().size()))
            .withChildren("building_info", buildingInfo)
            .withChildren("citizen_info", citizenInfo);

        return HtmlParser.parseHtml(TEMPLATE_MARKER_COLONY, markerContext).build();
    }

    /**
     * Internal method for getting the correct icon name for the CSS class for the marker.
     *
     * @param colony the target colony.
     * @return the identifier for the correct CSS class.
     */
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

    /**
     * Creates the shape for the colony marker.
     *
     * @param colony the target colony.
     * @return the shape instance.
     */
    public static Shape createShape(final IColony colony)
    {
        final Collection<ChunkPos> claimedChunks = ColonyChunkClaimCalculator.getAllClaimedChunks(colony);
        final ColonyArea area = AreaGenerator.generateAreaFromChunks(claimedChunks);
        return Shape.builder().addPoints(area.getPoints()).build();
    }

    /**
     * Creates the line color for the colony marker.
     *
     * @param colony the target colony.
     * @return the color instance.
     */
    public static Color getLineColor(final IColony colony)
    {
        final Color baseColor = new Color(Optional.ofNullable(colony.getTeamColonyColor()).map(ChatFormatting::getColor).orElse(0));
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), COLONY_MARKER_LINE_COLOR_ALPHA);
    }

    /**
     * Creates the fill color for the colony marker.
     *
     * @param colony the target colony.
     * @return the color instance.
     */
    public static Color getFillColor(final IColony colony)
    {
        final Color baseColor = new Color(Optional.ofNullable(colony.getTeamColonyColor()).map(ChatFormatting::getColor).orElse(0));
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), COLONY_MARKER_FILL_COLOR_ALPHA);
    }
}
