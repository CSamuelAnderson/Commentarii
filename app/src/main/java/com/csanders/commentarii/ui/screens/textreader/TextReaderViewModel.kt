package com.csanders.commentarii.ui.screens.textreader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.csanders.commentarii.R
import com.csanders.commentarii.datamodel.Section
import com.csanders.commentarii.datamodel.Section2
import com.csanders.commentarii.datamodel.Work
import com.csanders.commentarii.utilities.TEIWorkParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TextReaderViewModel @Inject constructor() : ViewModel() {
    val semanticName = "text reader screen"

    @Composable
    fun getWork(): Work {
        val parser = TEIWorkParser()
        return parser.getWorkFromResource(R.raw.plato_symposium_grc)
    }

    //Todo: Eventually we'll want getText running in the background or something.
    @Composable
    fun displaySection(section: Section2): AnnotatedString {
        return section.printedString
    }

    @Composable
    fun displayTitle(work: Work): AnnotatedString {
        return buildAnnotatedString {
            pushStyle(SpanStyle(fontSize = 24.sp))
            append(work.header.title)
            pop()
            append("\n")
            append(work.header.author)
            append("\n")
            append(work.header.languagesUsed.joinToString(", "))
        }
    }

    //Todo: we'll want to make Lists of sections their own class to improve readability and to avoid having to do this weird logic here
    fun getTOC(work: Work): List<Section2> {
        return work.text.fold(listOf(Section2())) { acc, section2 ->
            when (section2.isStartOfMajorSection) {
                true -> acc + section2
                false -> {
                    val changedString = acc.last().printedString + section2.printedString
                    val changedSection = Section2(changedString, acc.last().isStartOfMajorSection, acc.last().footnotes)
                    acc.dropLast(1)  + changedSection
                }
            }
        }

    }

    //Probably should be a tailrec call
    private fun flattenTree(section: Section): List<Section> {
        return listOf(section) + section.subsections.filter { it.text.isEmpty() }
            .flatMap { subsection ->
                flattenTree(subsection)
            }
    }

    private fun blobAllTextTogether(section: Section): String {

        tailrec fun helpBlob(acc: StringBuilder, stack: List<Section>): String {
            //If the stack is empty, then we are done.
            if (stack.isEmpty()) {
                return acc.toString()
            }

            //If it isn't, then add the current section's text to our accumulator
            val subsection = stack.last()
            if (subsection.text.isNotBlank()) {
                acc.append(subsection.text)
            }

            //And then add the current section's subsections to the stack
            //Todo: pretty big performance issue to both 1) call a reversed here, and 2) just generate a new list when we want.
            val newStack = stack.dropLast(1) + subsection.subsections.reversed()
            return helpBlob(acc, newStack)
        }
        return helpBlob(StringBuilder(), listOf(section))
    }

    companion object {
        const val route = "text_reader"
    }
}