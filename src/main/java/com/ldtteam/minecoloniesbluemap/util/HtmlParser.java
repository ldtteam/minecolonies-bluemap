package com.ldtteam.minecoloniesbluemap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public class HtmlParser
{
    private HtmlParser()
    {
    }

    public static String parseHtmlFile(final ResourceLocation file, final Map<String, String> context)
    {
        final Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
        if (resource.isPresent())
        {
            try
            {
                String htmlString = IOUtils.toString(resource.get().open(), Charset.defaultCharset());
                for (final Map.Entry<String, String> entry : context.entrySet())
                {
                    htmlString = htmlString.replaceAll("\\{\\{%s\\}\\}".formatted(entry.getKey()), entry.getValue());
                }
                return htmlString;
            }
            catch (Exception ex)
            {
                Log.getLogger().error("Failure parsing HTML file", ex);
            }
        }

        return "";
    }
}
