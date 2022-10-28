package com.csanders.commentarii.utilities

import android.content.res.Resources.NotFoundException
import android.util.Xml
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.csanders.commentarii.datamodel.Section
import com.csanders.commentarii.datamodel.Work
import com.csanders.commentarii.datamodel.WorkHeader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


class TEIParser() {

    //todo: we may need to change this to the TEI namespace.
    private val ns: String? = null

    //composable so we can grab the context
    @Composable
    @Throws(XmlPullParserException::class, IOException::class, NotFoundException::class)
    fun parseFromResource(resourceID: Int): List<Work> {
        val context = LocalContext.current
        val stream = context.resources.openRawResource(resourceID)
        return parse(stream)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Work> {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            //Todo: If (TEI), read work, if (TEICorpus), read many works, else return empty work or error
            return readWork(parser)
        }
    }

    private fun readWork(parser: XmlPullParser): List<Work> {
        var header = WorkHeader()

        //Todo: since a lower-level function returned a data-class instead of a map, we save off the class and just ignore that it returned a map.
        //Todo: Fine for now, but is there a more elegant way?
        parser.loop(TEIElement.TEI.element) { tag ->
            when (tag) {
                TEIElement.TeiHeader.element -> {
                    header = readHeader(parser)
                    mapOf()
                }
                else -> parser.skip(tag)
            }
        }
        val work = Work(header, Section())
        return listOf(work)
    }

    //parses the start tag, and don't ask it to save any data.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.skip(tag: String): Map<String, List<String>> {
        this.loop(tag)
        return mapOf()
    }

    //Currying function so we can refer to the one above without changing the signature
    //parses the start tag, and don't ask it to save any data.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser): (String) -> Map<String, List<String>> {
        return { parser.skip(it) }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun XmlPullParser.loop(
        startTag: String,
        onTagRead: (String) -> Map<String, List<String>> = { mapOf() }
    ): Map<String, List<String>> {
        this.require(XmlPullParser.START_TAG, ns, startTag)
        val map = mutableMapOf<String, List<String>>()

        tailrec fun helper(
            parser: XmlPullParser,
            parsedElements: MutableMap<String, List<String>> = mutableMapOf()
        ): Map<String, List<String>> {

            if (parser.name == startTag && parser.eventType == XmlPullParser.END_TAG) {
                return parsedElements.toMap()
            }
            //Skip at least once so that we don't need to handle the start tag in the lambda
            parser.next()
            if (parser.eventType == XmlPullParser.START_TAG) {
                parsedElements.mergeAll(onTagRead(parser.name))
            }

            return helper(parser, parsedElements)
        }
        return helper(this, map)
    }

    private fun <A, B> MutableMap<A, List<B>>.mergeAll(map: Map<A, List<B>>) {
        map.entries.forEach { entry ->
            this.merge(entry.key, entry.value) { old, new ->
                val coolList = mutableListOf<B>()
                coolList.addAll(old)
                coolList.addAll(new)
                coolList.toList()
            }
        }
    }

    //Parses the contents of a teiHeader.
    //TODO: Less duplicate code (skip has been abstracted), but no doubt harder to read
    //TODO: I don't think a map is what someone would expect to carry lambdas like this
    //TODO: What if you curried instead?
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readHeaderRefactor(parser: XmlPullParser): WorkHeader {
        var languageUsed: Map<String, List<String>> = mapOf()
        val map = parser.loop(TEIElement.TeiHeader.element) { tagInHeader ->
            readHeaderHelper(
                parser, tagInHeader, mapOf(
                    Pair(TEIHeader.FileDescription.element) {
                        parser.loop(tagInHeader) { tagInFile ->
                            readHeaderHelper(parser, tagInFile, mapOf(
                                Pair(TEIHeader.TitleStatement.element) {
                                    parser.loop(tagInHeader) { tagInTitleStatement ->
                                        languageUsed =
                                            readHeaderHelper(parser, tagInTitleStatement, mapOf(
                                                Pair(TEIHeader.Title.element) {
                                                    parser.readTag(
                                                        tagInTitleStatement
                                                    )
                                                },
                                                Pair(TEIHeader.Author.element) {
                                                    parser.readTag(
                                                        tagInTitleStatement
                                                    )
                                                }
                                            ))
                                        mapOf()
                                    }
                                }
                            ))
                        }
                    },
                    Pair(TEIHeader.ProfileDescription.element) {
                        parser.loop(tagInHeader) { tagInProfile ->
                            readHeaderHelper(parser, tagInProfile, mapOf(
                                Pair(TEIHeader.LanguagesUsed.element) {
                                    parser.loop(tagInProfile) { tagInLanguagesUsed ->
                                        readHeaderHelper(parser, tagInLanguagesUsed, mapOf(
                                            Pair(TEIHeader.Language.element) {
                                                parser.readTag(
                                                    tagInLanguagesUsed
                                                )
                                            }
                                        ))
                                    }
                                }
                            ))
                        }
                    })
            )
        }

        return WorkHeader(
            title = map.getOrDefault(TEIHeader.Title.element, listOf("Unknown Work"))
                .first(),
            author = map.getOrDefault(TEIHeader.Author.element, listOf("Unknown author"))
                .first(),
            languagesUsed = languageUsed.getOrDefault(
                TEIHeader.Language.element,
                listOf("Unknown languages")
            )
        )
    }

    private fun readHeaderHelper(
        parser: XmlPullParser,
        tag: String,
        tagMap: Map<String, (String) -> Map<String, List<String>>>
    ): Map<String, List<String>> {
        return tagMap.getOrDefault(tag, skip(parser))(tag)

    }


    //Parses the contents of a teiHeader.
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readHeader(parser: XmlPullParser): WorkHeader {
        var languageUsed: Map<String, List<String>> = mapOf()
        val map = parser.loop(TEIElement.TeiHeader.element) { tagInHeader ->

            //Todo: we can see that the current way of doing things duplicates a lot. Can we shorten this?
            when (tagInHeader) {
                TEIHeader.FileDescription.element -> parser.loop(tagInHeader) { tagInFileDesc ->
                    when (tagInFileDesc) {
                        TEIHeader.TitleStatement.element -> parser.loop(tagInFileDesc) { tagInTitleStatement ->
                            when (tagInTitleStatement) {
                                TEIHeader.Title.element -> parser.readTag(tagInTitleStatement)
                                TEIHeader.Author.element -> parser.readTag(tagInTitleStatement)
                                else -> parser.skip(tagInTitleStatement)
                            }
                        }
                        else -> parser.skip(tagInFileDesc)
                    }
                }
                TEIHeader.ProfileDescription.element -> parser.loop(
                    tagInHeader
                ) { tagInProfileDesc ->
                    when (tagInProfileDesc) {
                        TEIHeader.LanguagesUsed.element -> {
                            languageUsed = parser.loop(
                                tagInProfileDesc
                            ) { tagInLanguage ->
                                when (tagInLanguage) {
                                    TEIHeader.Language.element -> parser.readTag(tagInLanguage)
                                    else -> parser.skip(tagInLanguage)
                                }
                            }
                            mapOf()
                        }
                        else -> parser.skip(tagInProfileDesc)
                    }
                }
                else -> parser.skip(tagInHeader)
            }
        }
        return WorkHeader(
            title = map.getOrDefault(TEIHeader.Title.element, listOf("Unknown Work"))
                .first(),
            author = map.getOrDefault(TEIHeader.Author.element, listOf("Unknown author"))
                .first(),
            languagesUsed = languageUsed.getOrDefault(
                TEIHeader.Language.element,
                listOf("Unknown languages")
            )
        )
    }


    /**
     * Parses a single
     * Side effects: Moves the parser into the tag body, reads, and then moves it to the tag close.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun XmlPullParser.readTag(tag: String): Map<String, List<String>> {
        this.require(XmlPullParser.START_TAG, ns, tag)
        val decodedString = readText(this)
        this.require(XmlPullParser.END_TAG, ns, tag)
        return mapOf(Pair(tag, decodedString))
    }

    // Processes link tags in the feed.
//Todo: Google's example for using tags. We'll want to copy this and then discard later.
//    @Throws(IOException::class, XmlPullParserException::class)
//    private fun readLink(parser: XmlPullParser): String {
//        var link = ""
//        parser.require(XmlPullParser.START_TAG, ns, "link")
//        val tag = parser.name
//        val relType = parser.getAttributeValue(null, "rel")
//        if (tag == "link") {
//            if (relType == "alternate") {
//                link = parser.getAttributeValue(null, "href")
//                parser.nextTag()
//            }
//        }
//        parser.require(XmlPullParser.END_TAG, ns, "link")
//        return link
//    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): List<String> {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return listOf(result)
    }

}