package com.ldtteam.minecoloniesbluemap.integration;

import com.ldtteam.minecoloniesbluemap.util.FileLoader;
import com.ldtteam.minecoloniesbluemap.util.Log;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class BlueMapAssetManager
{
    /**
     * Asset identifiers.
     */
    private static final String CSS_MINECOLONIES       = "css/minecolonies.css";
    private static final String SCRIPTS_MINECOLONIES   = "scripts/minecolonies.js";
    private static final String TEXTURE_ARROW          = "textures/arrow.svg";
    private static final String TEXTURE_COLONY_CITY    = "textures/colony_city.png";
    private static final String TEXTURE_COLONY_VILLAGE = "textures/colony_village.png";
    private static final String TEXTURE_COLONY_HAMLET  = "textures/colony_hamlet.png";
    private static final String TEXTURE_COLONY_OUTPOST = "textures/colony_outpost.png";

    private BlueMapAssetManager() {}

    public static void createAssets(final @NotNull BlueMapAPI api)
    {
        try
        {
            Log.getLogger().info("[Bluemap] Clearing old files");
            final Path rootPath = api.getWebApp().getWebRoot().resolve(Path.of("assets", com.minecolonies.api.util.constant.Constants.MOD_ID));
            if (Files.exists(rootPath))
            {
                try (Stream<Path> walk = Files.walk(rootPath))
                {
                    walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                }
            }
            Files.createDirectories(rootPath);

            Log.getLogger().info("[Bluemap] Uploading new assets");
            copyFile(CSS_MINECOLONIES, rootPath);
            copyFile(SCRIPTS_MINECOLONIES, rootPath);
            copyFile(TEXTURE_ARROW, rootPath);
            copyFile(TEXTURE_COLONY_CITY, rootPath);
            copyFile(TEXTURE_COLONY_VILLAGE, rootPath);
            copyFile(TEXTURE_COLONY_HAMLET, rootPath);
            copyFile(TEXTURE_COLONY_OUTPOST, rootPath);

            Log.getLogger().info("[Bluemap] Registering styles and scripts");
            api.getWebApp().registerStyle(String.format("assets/%s/%s", com.minecolonies.api.util.constant.Constants.MOD_ID, CSS_MINECOLONIES));
            api.getWebApp().registerScript(String.format("assets/%s/%s", com.minecolonies.api.util.constant.Constants.MOD_ID, SCRIPTS_MINECOLONIES));
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Exception during uploading of assets", e);
        }
    }

    private static void copyFile(final String filePath, final Path targetDirectory) throws IOException
    {
        final Optional<InputStream> resource = FileLoader.openFile(String.format("web/%s", filePath));
        if (resource.isPresent())
        {
            final Path subPath = targetDirectory.resolve(filePath);
            Files.createDirectories(subPath.getParent());
            try (final InputStream input = resource.get();
                 final OutputStream output = Files.newOutputStream(subPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
            {
                IOUtils.copy(input, output);
            }
        }
    }
}
