package com.ldtteam.minecoloniesbluemap.util;

import com.ldtteam.minecoloniesbluemap.integration.BlueMapAssetManager;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Optional;

public class FileLoader
{
    /**
     * Obtain an {@link InputStream} to a file in the classpath {@code files} directory.
     *
     * @param resource the {@link ResourceLocation} to grab the path from.
     * @return the optional input stream.
     */
    public static Optional<InputStream> openFile(final ResourceLocation resource)
    {
        return Optional.ofNullable(BlueMapAssetManager.class.getClassLoader().getResourceAsStream(String.format("files/%s", resource.getPath())));
    }

    /**
     *
     * Obtain an {@link InputStream} to a file in the classpath {@code files} directory.
     *
     * @param filePath the file path.
     * @return the optional input stream.
     */
    public static Optional<InputStream> openFile(final String filePath)
    {
        return Optional.ofNullable(BlueMapAssetManager.class.getClassLoader().getResourceAsStream(String.format("files/%s", filePath)));
    }
}
