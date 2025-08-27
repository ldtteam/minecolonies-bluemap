package com.ldtteam.minecoloniesbluemap.integration;

import net.minecraft.resources.ResourceLocation;

public record MarkerIdentifier(
    ResourceLocation markerSet,
    int colonyId,
    int itemId)
{
    public String getMarkerSetId()
    {
        return markerSet.toString();
    }

    public String getMarkerId()
    {
        return "%d:%d".formatted(colonyId, itemId);
    }
}
