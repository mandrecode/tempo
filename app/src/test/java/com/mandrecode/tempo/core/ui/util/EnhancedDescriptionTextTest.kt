package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EnhancedDescriptionTextTest {
    @Test
    fun givenBareWebLink_whenNormalizingDescriptionUri_thenAddsHttpsScheme() {
        assertThat(normalizeDescriptionUri("www.example.com"))
            .isEqualTo("https://www.example.com")
    }

    @Test
    fun givenBareDomain_whenNormalizingDescriptionUri_thenAddsHttpsScheme() {
        assertThat(normalizeDescriptionUri("google.es"))
            .isEqualTo("https://google.es")
    }

    @Test
    fun givenHttpsUri_whenNormalizingDescriptionUri_thenPreservesUri() {
        assertThat(normalizeDescriptionUri("https://example.com/docs"))
            .isEqualTo("https://example.com/docs")
    }

    @Test
    fun givenSupportedWebUrls_whenBuildingEnhancedDescriptionText_thenAddsAnnotationsInOrder() {
        val text =
            "Open https://example.com, http://example.net, www.example.org, and google.es."

        val annotations =
            buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)
                .getStringAnnotations(start = 0, end = text.length)
                .map { it.item }

        assertThat(annotations)
            .containsExactly(
                "https://example.com",
                "http://example.net",
                "https://www.example.org",
                "https://google.es",
            ).inOrder()
    }

    @Test
    fun givenNonWebUris_whenBuildingEnhancedDescriptionText_thenDoesNotAddLinkAnnotations() {
        val text =
            "Skip mailto:hello@example.com tel:+34123456789 geo:40.4,-3.7 " +
                "content://provider/item file:///storage/emulated/0/Documents/note.pdf"

        val enhancedText = buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)

        assertThat(enhancedText.getStringAnnotations(start = 0, end = text.length)).isEmpty()
        assertThat(enhancedText.spanStyles).isEmpty()
    }

    @Test
    fun givenPlainDescription_whenBuildingEnhancedDescriptionText_thenDoesNotAddLinkAnnotations() {
        val text = "This is a normal note without references."

        val enhancedText = buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)

        assertThat(enhancedText.text).isEqualTo(text)
        assertThat(enhancedText.getStringAnnotations(start = 0, end = text.length)).isEmpty()
        assertThat(enhancedText.spanStyles).isEmpty()
    }

    @Test
    fun givenDescriptionWithoutDomainDot_whenBuildingEnhancedDescriptionText_thenDoesNotAddLinkAnnotations() {
        val text = "Open google when planning."

        val enhancedText = buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)

        assertThat(enhancedText.getStringAnnotations(start = 0, end = text.length)).isEmpty()
        assertThat(enhancedText.spanStyles).isEmpty()
    }

    @Test
    fun givenBareEmailAddress_whenBuildingEnhancedDescriptionText_thenDoesNotLinkDomainSegment() {
        val text = "Email hello@example.com later."

        val enhancedText = buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)

        assertThat(enhancedText.getStringAnnotations(start = 0, end = text.length)).isEmpty()
        assertThat(enhancedText.spanStyles).isEmpty()
    }

    @Test
    fun givenMixedDescription_whenBuildingEnhancedDescriptionText_thenStylesOnlyTheLinkRange() {
        val text = "Read https://example.com/docs before starting"
        val link = "https://example.com/docs"
        val linkStart = text.indexOf(link)
        val linkEnd = linkStart + link.length

        val enhancedText = buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)
        val annotation = enhancedText.getStringAnnotations(start = 0, end = text.length).single()
        val spanStyle = enhancedText.spanStyles.single()

        assertThat(enhancedText.text).isEqualTo(text)
        assertThat(annotation.start).isEqualTo(linkStart)
        assertThat(annotation.end).isEqualTo(linkEnd)
        assertThat(annotation.item).isEqualTo(link)
        assertThat(spanStyle.start).isEqualTo(linkStart)
        assertThat(spanStyle.end).isEqualTo(linkEnd)
        assertThat(spanStyle.item.color).isEqualTo(Color.Blue)
    }

    @Test
    fun givenLinkWithTrailingSentencePunctuation_whenBuildingEnhancedDescriptionText_thenTrimsAnnotationRange() {
        val text = "Read https://example.com/docs."

        val annotation =
            buildEnhancedDescriptionText(text = text, linkColor = Color.Blue)
                .getStringAnnotations(start = 0, end = text.length)
                .single()

        assertThat(annotation.item).isEqualTo("https://example.com/docs")
        assertThat(text.substring(annotation.start, annotation.end))
            .isEqualTo("https://example.com/docs")
    }

    @Test
    fun givenInitialLinkStyles_whenTransformingCurrentText_thenPreservesTextAndOffsets() {
        val currentText = AnnotatedString("Read https://example.com")
        val transformation =
            IncrementalLinkVisualTransformation(
                initialText = currentText.text,
                linkColor = Color.Blue,
            )

        val transformedText = transformation.filter(currentText)

        assertThat(transformedText.text.text).isEqualTo(currentText.text)
        assertThat(styledSubstrings(transformedText.text)).containsExactly("https://example.com")
        assertThat(transformedText.offsetMapping.originalToTransformed(currentText.length))
            .isEqualTo(currentText.length)
    }

    @Test
    fun givenTextInsertedBetweenLinks_whenUpdating_thenKeepsBothLinksStyledInPlace() {
        val initialText = "Open google.es and youtube.com"
        val updatedText = "Open google.es and write here before youtube.com"
        val transformation =
            IncrementalLinkVisualTransformation(
                initialText = initialText,
                linkColor = Color.Blue,
            )

        transformation.update(updatedText)
        val transformedText = transformation.filter(AnnotatedString(updatedText))

        assertThat(styledSubstrings(transformedText.text))
            .containsExactly("google.es", "youtube.com")
            .inOrder()
    }

    @Test
    fun givenMultilinePasteBeforeLaterLink_whenUpdating_thenShiftsAndStylesAllLinks() {
        val initialText = "google.es\nyoutube.com"
        val updatedText = "google.es\nexample.org\nyoutube.com"
        val transformation =
            IncrementalLinkVisualTransformation(
                initialText = initialText,
                linkColor = Color.Blue,
            )

        transformation.update(updatedText)
        val transformedText = transformation.filter(AnnotatedString(updatedText))

        assertThat(styledSubstrings(transformedText.text))
            .containsExactly("google.es", "example.org", "youtube.com")
            .inOrder()
    }

    @Test
    fun givenEditInvalidatesLink_whenUpdating_thenRemovesOnlyThatLinkStyle() {
        val initialText = "google.es\nyoutube.com"
        val updatedText = "googlees\nyoutube.com"
        val transformation =
            IncrementalLinkVisualTransformation(
                initialText = initialText,
                linkColor = Color.Blue,
            )

        transformation.update(updatedText)
        val transformedText = transformation.filter(AnnotatedString(updatedText))

        assertThat(styledSubstrings(transformedText.text)).containsExactly("youtube.com")
    }

    @Test
    fun givenMixedEditSequence_whenUpdatingIncrementally_thenMatchesFullLinkParsing() {
        val versions =
            listOf(
                "google.es and youtube.com\nexample.org",
                "google.es and notes before youtube.com\nexample.org",
                "googlees and notes before youtube.com\nexample.org",
                "googlees and notes before youtube.com\nnewsite.net\nexample.org",
                "googlees and notes before youtube.com\nnewsite.net example.org",
            )
        val transformation =
            IncrementalLinkVisualTransformation(
                initialText = versions.first(),
                linkColor = Color.Blue,
            )

        versions.forEach { version ->
            transformation.update(version)
            val incrementalText = transformation.filter(AnnotatedString(version)).text
            val fullyParsedText = buildEnhancedDescriptionText(text = version, linkColor = Color.Blue)

            assertThat(incrementalText.spanStyles)
                .containsExactlyElementsIn(fullyParsedText.spanStyles)
                .inOrder()
        }
    }

    private fun styledSubstrings(text: AnnotatedString): List<String> =
        text.spanStyles.map { range -> text.text.substring(range.start, range.end) }
}
