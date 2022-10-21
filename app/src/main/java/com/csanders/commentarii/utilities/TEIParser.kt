package com.csanders.commentarii.utilities

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory;

class TEIParser() {
    private val xpp: XmlPullParser;

    init {
        xpp = setupXmlParser()
    }

    private fun setupXmlParser(): XmlPullParser {
        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        return factory.newPullParser()
    }

    fun parseWork() {

    }




}