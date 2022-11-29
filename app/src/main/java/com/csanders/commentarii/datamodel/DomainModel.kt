package com.csanders.commentarii.datamodel

import androidx.compose.ui.text.TextStyle
import com.csanders.commentarii.ui.theme.Typography

/**
 * The domain I'm trying to model is one which an expert in Classics, books, and formatting would be accepted in.
 * The goal is to use stuff like value classes, sealed classes, and Tuples to give a coherent model of the domain that the
 * compile can check against
 *
 * At bottom level, we're just identifying certain domain topics as a set where each member meets a certain condition.
 *
 * We'll of course continue as we go along
 */

/**
 * Event Storming: Certain things that can happen in this domain
 *  1. A user wants to check out a book - add a bookId to Library
 *  2. Open a book
 *  3. Flip a page
 *  4. Highlight a section
 *  4a Write in the margins
 *  5. Review a vocab note
 *  6. Review reference material
 *  7. Return to a table of contents
 *  8. Jump to a section
 *  9. Search for a passage they remember
 *  10. Use flashcards
 *  11. Revise some of the metadata, reorganize the material
 *  12. Revise parsing inaccuracies
 *  13. Adjust font and usability settings
 *  14. A user wants to import a book
 *  15. A user wants to export a book
 *  16. A user wants to export their vocab as flashcards
 */

//A book is something you can reference and review.
data class Book(
    val id: BunkID = -1,
    //TODO: Should be replaced with the ID later
    //No table of content for now
//    val tableOfContentsId: TableOfContents,
    val pages: Pages,
    val header: Header
)

//The Header is whatever information we grab from the Xml file but isn't in the body of the text itself.
data class Header(
    val title: Title,
    val author: Author,
    val languagesUsed: List<Language>
)

//Todo: eventually add a restricted setter to avoid making too long a name
@JvmInline
value class Author(val value: String)

@JvmInline
value class Language(val value: String)

@JvmInline
value class Title(val value: String)

//Todo: Eventually, we'll want to make these generate automatically
data class BookId(val id: BunkID)

//Refers to the currently opened page, and can be moved back and forth between chapters
//Eventually, we'll want to make a LinkedList data structure so that we don't create new lists every page turn
//Should be IDs after this.
//Todo: Open/Closed and previous/future is a property of all pages, not just chapters.
data class Pages(
    val pagesID: BunkID = -1,
    val openedPage: Page,
    val previousPages: List<Page>,
    val futurePages: List<Page>,
)

/** A page represents any unit of text that should be displayed at a single time
 */
sealed class Page(
    val id: BunkID,
    val passages: List<Passage>,
)

class TableOfContents(passages: List<Passage>, id: BunkID = -1) : Page(id, passages)
class Chapter(val chapterHeading: ChapterHeading, passages: List<Passage>, id: BunkID = -1) :
    Page(id, passages)

//Todo: Styling will eventually need its own class, since we want the user to be able to configure it, we don't want to save TextStyle right away
/**
 * A section is a piece of text alongside the styling to be used when displaying it.
 */
sealed class Section(
    val styling: TextStyle,
    val text: String
)

class ChapterHeading(text: String) : Section(Typography.titleMedium, text)


class Passage(text: String, styling: TextStyle = Typography.bodyMedium) :
    Section(styling, text)



/**
 * Ok, let me back up: what are the domain rules for adding marginalia (highlighting, vocab, footnote)?
 *      It has visual indication
 *      The visual indication is bounded -- highlight is bounded on two words (within the same Page), vocab and footnotes are bounded to a single word.
 *      The display is chunked by Section, which are based on styling, but marginalia are chunked by the word.
 *      We'll want functions that can map a word to its count on the page, and a word count to a particular change in the displayed text
 *      So the workflow goes
 *
 */

sealed class Marginalia

/**
 * setting up an Undefined type so we can continue to model without knowing the guts of the types.
 */
typealias Undefined = Exception
typealias BunkID = Int