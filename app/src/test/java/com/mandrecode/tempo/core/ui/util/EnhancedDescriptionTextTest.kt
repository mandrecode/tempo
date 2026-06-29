package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    fun givenTextFieldValueWithLink_whenEnhancingDescriptionTextFieldValue_thenPreservesSelectionAndStylesLink() {
        val value =
            TextFieldValue(
                text = "Open www.example.com",
                selection = TextRange(8),
            )

        val enhancedValue = enhancedDescriptionTextFieldValue(value = value, linkColor = Color.Blue)
        val linkStyleColor =
            enhancedValue.annotatedString.spanStyles
                .single()
                .item.color

        assertThat(enhancedValue.text).isEqualTo(value.text)
        assertThat(enhancedValue.selection).isEqualTo(TextRange(8))
        assertThat(enhancedValue.annotatedString.spanStyles).hasSize(1)
        assertThat(linkStyleColor).isEqualTo(Color.Blue)
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
}
