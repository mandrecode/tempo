package com.mandrecode.tempo.core.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration

internal class IncrementalLinkVisualTransformation(
    initialText: String,
    linkColor: Color,
) : VisualTransformation {
    private var sourceText = initialText
    private var links = findDescriptionLinks(initialText).toList()
    private val linkStyle =
        SpanStyle(
            color = linkColor,
            textDecoration = TextDecoration.Underline,
        )

    fun update(text: String) {
        if (text == sourceText) return

        links = updateDescriptionLinks(previousText = sourceText, text = text, links = links)
        sourceText = text
    }

    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text != sourceText) {
            return TransformedText(
                text = text,
                offsetMapping = OffsetMapping.Identity,
            )
        }

        val transformedText =
            buildAnnotatedString {
                append(text)
                links.forEach { link ->
                    addStyle(
                        style = linkStyle,
                        start = link.range.first,
                        end = link.range.last + 1,
                    )
                }
            }

        return TransformedText(
            text = transformedText,
            offsetMapping = OffsetMapping.Identity,
        )
    }
}

private fun updateDescriptionLinks(
    previousText: String,
    text: String,
    links: List<DescriptionLink>,
): List<DescriptionLink> {
    val commonPrefixLength = commonPrefixLength(previousText, text)
    val commonSuffixLength = commonSuffixLength(previousText, text, commonPrefixLength)
    val previousChangeEnd = previousText.length - commonSuffixLength
    val changeEnd = text.length - commonSuffixLength
    val previousParagraphStart = paragraphStart(previousText, commonPrefixLength)
    val previousParagraphEnd = paragraphEnd(previousText, previousChangeEnd)
    val paragraphStart = paragraphStart(text, commonPrefixLength)
    val paragraphEnd = paragraphEnd(text, changeEnd)
    val lengthDelta = text.length - previousText.length

    val linksBeforeChange = links.filter { it.range.last < previousParagraphStart }
    val linksInChangedParagraphs =
        findDescriptionLinks(
            text = text.substring(paragraphStart, paragraphEnd),
            rangeOffset = paragraphStart,
        ).toList()
    val linksAfterChange =
        links
            .filter { it.range.first >= previousParagraphEnd }
            .map { link ->
                link.copy(
                    range =
                        (link.range.first + lengthDelta)..(link.range.last + lengthDelta),
                )
            }

    return linksBeforeChange + linksInChangedParagraphs + linksAfterChange
}

private fun commonPrefixLength(
    first: String,
    second: String,
): Int {
    val maximumLength = minOf(first.length, second.length)
    var length = 0
    while (length < maximumLength && first[length] == second[length]) {
        length++
    }
    return length
}

private fun commonSuffixLength(
    first: String,
    second: String,
    commonPrefixLength: Int,
): Int {
    val maximumLength = minOf(first.length, second.length) - commonPrefixLength
    var length = 0
    while (length < maximumLength && first[first.lastIndex - length] == second[second.lastIndex - length]) {
        length++
    }
    return length
}

private fun paragraphStart(
    text: String,
    changeStart: Int,
): Int {
    if (changeStart == 0) return 0
    return text.lastIndexOf('\n', startIndex = changeStart - 1) + 1
}

private fun paragraphEnd(
    text: String,
    changeEnd: Int,
): Int = text.indexOf('\n', startIndex = changeEnd).takeIf { it >= 0 } ?: text.length
