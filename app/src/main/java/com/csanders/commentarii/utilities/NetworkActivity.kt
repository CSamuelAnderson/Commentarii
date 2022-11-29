package com.csanders.commentarii.utilities

import android.app.Activity
import android.os.AsyncTask
import com.csanders.commentarii.datamodel.Book
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.String


//Copy of https://developer.android.com/training/basics/network-ops/xml
//TODO: Will one day be specific to my thing
class NetworkActivity : Activity() {

    companion object {

        const val WIFI = "Wi-Fi"
        const val ANY = "Any"
        const val SO_URL = "http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest"
        // Whether there is a Wi-Fi connection.
        private var wifiConnected = false
        // Whether there is a mobile connection.
        private var mobileConnected = false

        // Whether the display should be refreshed.
        var refreshDisplay = true
        // The user's current network preference setting.
        var sPref: String? = null
    }


    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    // Uses AsyncTask to download the XML feed from stackoverflow.com.
    fun loadPage() {

        if (sPref.equals(ANY) && (wifiConnected || mobileConnected)) {
            DownloadXmlTask().execute(SO_URL)
        } else if (sPref.equals(WIFI) && wifiConnected) {
            DownloadXmlTask().execute(SO_URL)
        } else {
            // show error
        }
    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    //TODO: Async task is deprecated. We'd want ares to use coroutines.
    private inner class DownloadXmlTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String): String {
            return try {
                loadXmlFromNetwork(urls[0])
            } catch (e: IOException) {
                "oops!"
                //resources.getString(R.string.connection_error)
            } catch (e: XmlPullParserException) {
                "oops!"
//               resources.getString(R.string.xml_error)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String) {
            //We'd want this to trigger a database storage.
           // setContentView(R.layout.main)
        }

        // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
        @Throws(XmlPullParserException::class, IOException::class)
        private fun loadXmlFromNetwork(urlString: String): String {
            // Checks whether the user set the preference to include summary text
            //TODO: We can ignore user preferences
            val pref: Boolean = false
//            val pref: Boolean = PreferenceManager.getDefaultSharedPreferences(this)?.run {
//                getBoolean("summaryPref", false)
//            } ?: false


            //todo: we'll want this to be our TEIParser
            val entries: List<Book> = downloadUrl(urlString)?.use { stream: InputStream ->
                // Instantiate the parser
                //StackOverflowXmlParser().parse(stream)
                emptyList()
            } ?: emptyList()

            //TODO: composables use annotated strings, which we can use in order to gather this info.
            return StringBuilder().apply {
//                append("<h3>${resources.getString(R.string.page_title)}</h3>")
//                append("<em>${resources.getString(R.string.updated)} ")
//                append("${formatter.format(rightNow.time)}</em>")
                // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
                // Each Entry object represents a single post in the XML feed.
                // This section processes the entries list to combine each entry with HTML markup.
                // Each entry is displayed in the UI as a link that optionally includes
                // a text summary.
//                entries.forEach { entry ->
//                    append("<p><a href='")
//                    append(entry.link)
//                    append("'>" + entry.title + "</a></p>")
                    // If the user set the preference to include summary text,
                    // adds it to the display.
//                }
            append("TODO")}.toString()

        }

        // Given a string representation of a URL, sets up a connection and gets
// an input stream.
        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream? {
            val url = URL(urlString)
            return (url.openConnection() as? HttpURLConnection)?.run {
                readTimeout = 10000
                connectTimeout = 15000
                requestMethod = "GET"
                doInput = true
                // Starts the query
                connect()
                inputStream
            }
        }

    }


}
