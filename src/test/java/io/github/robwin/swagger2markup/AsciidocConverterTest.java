/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.robwin.swagger2markup;

import io.github.robwin.markup.builder.LineSeparator;
import io.github.robwin.swagger2markup.assertions.DiffUtils;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.extension.Swagger2MarkupExtensionRegistry;
import io.github.robwin.swagger2markup.extension.repository.DynamicDefinitionsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.DynamicOperationsContentExtension;
import io.github.robwin.swagger2markup.extension.repository.SchemaExtension;
import io.github.robwin.swagger2markup.extension.repository.SpringRestDocsExtension;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.BDDAssertions.assertThat;

public class AsciidocConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(AsciidocConverterTest.class);
    private static final String[] EXPECTED_FILES = new String[]{"definitions.adoc", "overview.adoc", "paths.adoc", "security.adoc"};
    private List<String> expectedFiles;
    
    @Before
    public void setUp(){
        expectedFiles = new ArrayList<>(asList(EXPECTED_FILES));
    }

    @Test
    public void testSwagger2AsciiDocConversionAsString() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());

        //When
        String asciiDocAsString = Swagger2MarkupConverter.from(file).build()
                .asString();
        //Then
        assertThat(asciiDocAsString).isNotEmpty();
    }


    @Test
    public void testSwagger2AsciiDocConversion() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withLineSeparator(LineSeparator.WINDOWS).build();

        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/default").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversion.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionFromString() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withLineSeparator(LineSeparator.WINDOWS).build();

        Swagger2MarkupConverter.from(swaggerJsonString).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/default").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversion.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSpringRestDocsExtension() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/yaml/swagger_petstore.yaml"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new SpringRestDocsExtension(Paths.get("src/docs/asciidoc/paths").toUri()).withDefaultSnippets())
                .build();

        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/spring_rest_docs").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversionWithSpringRestDocsExtension.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithExamples() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/examples").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversionWithExamples.html");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithGeneratedExamples() throws IOException, URISyntaxException {
        //Given
        String swaggerJsonString = IOUtils.toString(getClass().getResourceAsStream("/json/swagger_examples.json"));
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withGeneratedExamples()
                .build();

        Swagger2MarkupConverter.from(swaggerJsonString)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/generated_examples").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocConversionWithGeneratedExamples.html");
    }

    @Test
    public void testSwagger2AsciiDocWithInlineSchema() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withInlineSchemaDepthLevel(1)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTags() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());
        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
        Path expectedFilesDirectory = Paths.get(AsciidocConverterTest.class.getResource("/results/asciidoc/group_by_tags").toURI());
        DiffUtils.assertThatAllFilesAreEqual(outputDirectory, expectedFilesDirectory, "testSwagger2AsciiDocGroupedByTags.html");
    }

    @Test
    public void testSwagger2AsciiDocGroupedByTagsWithMissingTag() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/json/swagger_missing_tag.json").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());
        //When
        try {
            Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                    .withPathsGroupedBy(GroupBy.TAGS)
                    .build();

            Swagger2MarkupConverter.from(file)
                    .withConfig(config)
                    .build()
                    .intoFolder(outputDirectory);
            // If NullPointerException was not thrown, test would fail the specified message
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception e) {
            assertThat(e).hasMessage("Can't GroupBy.TAGS > Operation 'updatePet' has not tags");
        }
    }

    @Test
    public void testSwagger2AsciiDocConversionWithDefinitionDescriptions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withDefinitionDescriptions(Paths.get("src/docs/asciidoc/definitions"))
                .build();

        Swagger2MarkupConverter.from(file)
                .withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);
    }

    @Test
    public void testSwagger2AsciiDocConversionDoesNotContainUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_should_not_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .doesNotContain("=== URI scheme");
    }

    @Test
    public void testSwagger2AsciiDocConversionContainsUriScheme() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_should_contain_uri_scheme.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConverter.from(file).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        assertThat(files).hasSize(4).containsAll(expectedFiles);

        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc"))))
                .contains("=== URI scheme");
    }


    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedDefinitions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedDefinitions()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        expectedFiles.add("definitions");
        assertThat(files).hasSize(5).containsAll(expectedFiles);

        Path definitionsDirectory = outputDirectory.resolve("definitions");
        String[] definitions = definitionsDirectory.toFile().list();
        assertThat(definitions).hasSize(5).containsAll(
                asList("Category.adoc", "Order.adoc", "Pet.adoc", "Tag.adoc", "User.adoc"));
    }

    @Test
    public void testSwagger2AsciiDocConversionWithSeparatedOperations() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withSeparatedOperations()
                .build();
        Swagger2MarkupConverter.from(file).withConfig(config).build()
                .intoFolder(outputDirectory);

        //Then
        String[] files = outputDirectory.toFile().list();
        expectedFiles.add("operations");
        assertThat(files).hasSize(5).containsAll(expectedFiles);

        Path pathsDirectory = outputDirectory.resolve("operations");
        String[] paths = pathsDirectory.toFile().list();
        assertThat(paths).hasSize(18);
    }

    @Test
    public void testSwagger2AsciiDocConversionWithRussianOutputLanguage() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withOutputLanguage(Language.RU)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")), Charset.forName("UTF-8")))
                .contains("== Определения");
    }

    @Test
    public void testSwagger2AsciiDocConversionWithFrenchOutputLanguage() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .withOutputLanguage(Language.FR)
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("overview.adoc")), Charset.forName("UTF-8")))
                .contains("== Sch\u00E9ma d'URI");
    }

    @Test
    public void testSwagger2AsciiDocExtensions() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new DynamicDefinitionsContentExtension(Paths.get("src/docs/asciidoc/extensions")))
                .withExtension(new DynamicOperationsContentExtension(Paths.get("src/docs/asciidoc/extensions")))
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("paths.adoc")))).contains(
                "Pet update request extension");
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")))).contains(
                "Pet extension");

    }

    @Test
    public void testSwagger2AsciiDocSchemaExtension() throws IOException, URISyntaxException {
        //Given
        Path file = Paths.get(AsciidocConverterTest.class.getResource("/yaml/swagger_petstore.yaml").toURI());
        Path outputDirectory = Paths.get("build/docs/asciidoc/generated");
        FileUtils.deleteQuietly(outputDirectory.toFile());

        //When
        Swagger2MarkupConfig config = Swagger2MarkupConfig.ofDefaults()
                .build();
        Swagger2MarkupExtensionRegistry registry = Swagger2MarkupExtensionRegistry.ofEmpty()
                .withExtension(new SchemaExtension(Paths.get("src/docs/asciidoc/extensions").toUri()))
                .build();
        Swagger2MarkupConverter.from(file)
                .withConfig(config)
                .withExtensionRegistry(registry)
                .build()
                .intoFolder(outputDirectory);

        //Then
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")))).contains(
                "=== Pet");
        assertThat(new String(Files.readAllBytes(outputDirectory.resolve("definitions.adoc")))).contains(
                "==== XML Schema");
    }
}