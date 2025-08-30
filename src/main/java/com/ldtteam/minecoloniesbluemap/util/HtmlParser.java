package com.ldtteam.minecoloniesbluemap.util;

import com.google.common.html.HtmlEscapers;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class responsible for parsing HTML strings and files back to properly escaped HTML strings.
 * <p/>
 * Can handle variables using a {@code {{variable}}} format and child HTML using {@code {-child-}} replacements.
 */
public class HtmlParser
{
    /**
     * The regex format for replacing child blocks.
     */
    private static final String CHILD_FORMAT = "\\{\\-%s\\-\\}";

    /**
     * The regex format for replacing variables.
     */
    private static final String VARIABLE_FORMAT = "\\{\\{%s\\}\\}";

    private HtmlParser()
    {
    }

    /**
     * Parse a resource file to HTML text, given a parsing context.
     *
     * @param file    the target file to parse.
     * @param context the HTTP parsing context, containing all variables and children.
     * @return the parsed HTML, or an empty string if the file could not be loaded, or the parsing failed.
     */
    public static HttpContent parseHtml(@NotNull final ResourceLocation file, @NotNull final HttpParsingContext context)
    {
        final Optional<InputStream> resource = FileLoader.openFile(file);
        if (resource.isPresent())
        {
            try
            {
                final String htmlString = IOUtils.toString(resource.get(), Charset.defaultCharset());
                return parseHtml(htmlString, context);
            }
            catch (IOException ex)
            {
                Log.getLogger().error("Failure opening resource file for HTML parsing", ex);
            }
        }

        return new HttpContent("", Map.of());
    }

    /**
     * Parse a string to HTML text, given a parsing context.
     *
     * @param content the raw HTML string.
     * @param context the HTTP parsing context, containing all variables and children.
     * @return the parsed HTML, or an empty string if the parsing failed.
     */
    public static HttpContent parseHtml(@NotNull final String content, @NotNull final HttpParsingContext context)
    {
        try
        {
            String htmlString = content;
            for (final Map.Entry<String, Collection<HttpContent>> entry : context.children.entrySet())
            {
                final String childrenHtml = entry.getValue().stream().map(HttpContent::build).collect(Collectors.joining());
                htmlString = htmlString.replaceAll(CHILD_FORMAT.formatted(entry.getKey()), childrenHtml);
            }
            return new HttpContent(htmlString, context.variables);
        }
        catch (Exception ex)
        {
            Log.getLogger().error("Failure parsing HTML file", ex);
        }
        return new HttpContent("", Map.of());
    }

    /**
     * The HTTP parsing context contains the data like children and variables used during the parsing of the HTML.
     */
    public static class HttpParsingContext
    {
        /**
         * The map of children, found by key, containing the child HTTP content.
         */
        private final Map<String, Collection<HttpContent>> children = new HashMap<>();

        /**
         * The map of variables, found by key, containing the replacement values.
         */
        private final Map<String, String> variables = new HashMap<>();

        private HttpParsingContext()
        {
        }

        /**
         * Create a new, empty, HTTP parsing context.
         *
         * @return the parsing context builder.
         */
        public static HttpParsingContext builder()
        {
            return new HttpParsingContext();
        }

        /**
         * Add a new variable to this replacement context.
         *
         * @param key   the replacement key.
         * @param value the replacement value.
         * @return the parsing context builder.
         */
        public HttpParsingContext withVariable(final String key, final String value)
        {
            variables.put(key, value);
            return this;
        }

        /**
         * Add a new single child to this replacement context.
         *
         * @param key   the replacement key.
         * @param value the replacement child HTTP content.
         * @return the parsing context builder.
         */
        public HttpParsingContext withChild(final String key, final HttpContent value)
        {
            children.put(key, List.of(value));
            return this;
        }

        /**
         * Add a new multi child to this replacement context.
         *
         * @param key    the replacement key.
         * @param values the replacement child HTTP contents.
         * @return the parsing context builder.
         */
        public HttpParsingContext withChildren(final String key, final Collection<HttpContent> values)
        {
            children.put(key, values);
            return this;
        }
    }

    /**
     * This is a container class holding the HTTP content, with the raw HTML string and the replacement variables.
     * Parsing won't happen until {@link HttpContent#build()} is called, this object is not mutable.
     */
    @SuppressWarnings("ClassCanBeRecord")
    public static class HttpContent
    {
        /**
         * The raw HTML string.
         */
        private final String rawContent;

        /**
         * The replacement variables.
         */
        private final Map<String, String> variables;

        /**
         * Internal constructor.
         *
         * @param rawContent the raw HTML string.
         * @param variables  the replacement variables.
         */
        private HttpContent(final String rawContent, final Map<String, String> variables)
        {
            this.rawContent = rawContent;
            this.variables = variables;
        }

        /**
         * Turns this {@link HttpContent} instance into an escaped HTML string.
         *
         * @return the finalized HTML string.
         */
        public String build()
        {
            String htmlString = rawContent;
            for (final Map.Entry<String, String> entry : variables.entrySet())
            {
                htmlString = htmlString.replaceAll(VARIABLE_FORMAT.formatted(entry.getKey()), HtmlEscapers.htmlEscaper().escape(entry.getValue()));
            }
            return htmlString;
        }
    }
}
