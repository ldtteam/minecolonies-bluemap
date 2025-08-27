package com.ldtteam.minecoloniesbluemap.integration;

import com.ldtteam.minecoloniesbluemap.Constants;
import com.ldtteam.minecoloniesbluemap.util.Log;
import de.bluecolored.bluemap.api.BlueMapAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
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
    private static final ResourceLocation CSS_MINECOLONIES       = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "css/minecolonies.css");
    private static final ResourceLocation SCRIPTS_MINECOLONIES   = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "scripts/minecolonies.js");
    private static final ResourceLocation TEXTURE_ARROW          = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/arrow.svg");
    private static final ResourceLocation TEXTURE_COLONY_CITY    = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/colony_city.png");
    private static final ResourceLocation TEXTURE_COLONY_VILLAGE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/colony_village.png");
    private static final ResourceLocation TEXTURE_COLONY_HAMLET  = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/colony_hamlet.png");
    private static final ResourceLocation TEXTURE_COLONY_OUTPOST = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/colony_outpost.png");

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
            api.getWebApp().registerStyle(CSS_MINECOLONIES.withPrefix("assets/" + com.minecolonies.api.util.constant.Constants.MOD_ID + "/").getPath());
            api.getWebApp().registerScript(SCRIPTS_MINECOLONIES.withPrefix("assets/" + com.minecolonies.api.util.constant.Constants.MOD_ID + "/").getPath());
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Exception during uploading of assets", e);
        }
    }

    private static void copyFile(final ResourceLocation id, final Path targetDirectory) throws IOException
    {
        final Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id.withPrefix("web/"));
        if (resource.isPresent())
        {
            final Path subPath = targetDirectory.resolve(id.getPath());
            Files.createDirectories(subPath.getParent());
            try (final InputStream input = resource.get().open();
                 final OutputStream output = Files.newOutputStream(subPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
            {
                IOUtils.copy(input, output);
            }
        }
    }
}
