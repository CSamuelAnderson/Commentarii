package com.csanders.commentarii.utilities

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.util.Xml
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.csanders.commentarii.datamodel.Book
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream


class TEIWorkParser() {

    @Throws(XmlPullParserException::class, IOException::class, NotFoundException::class)
    fun getBookFromResource(resourceID: Int, context: Context): Book {
        val stream = context.resources.openRawResource(resourceID)
        return getBook(stream)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun getBook(inputStream: InputStream): Book {
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            //Todo: If (TEI), read work, if (TEICorpus), read many works, else return empty work or error
            val parsedXml = parser.parseTag(TEIElement.TEI.element)
            return convertToBook(parsedXml)
        }
    }
}