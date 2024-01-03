package com.example.unifiednews.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.StringReader
import org.jsoup.Jsoup
import java.net.URL
object RssFeedFetcher {

    private val client = OkHttpClient()

    fun fetchAndParseRssFeed(url: String, callback: (RssFeed?) -> Unit) {
        Thread {
            val rssFeedXml = fetchRssFeed(url)
            val rssFeed = parseXmlToRssFeed(rssFeedXml)
            callback(rssFeed)
        }.start()
    }

    private fun fetchRssFeed(url: String): String? {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        }
    }

    fun getPageIcon(url: String?, callback: (String?) -> Unit) {
        if(url == null) return
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val html = response.body?.string()
                    val doc = Jsoup.parse(html)

                    val icons = doc.head().getElementsByTag("link").filter {
                        it.attr("rel").contains("icon", ignoreCase = true)
                    }

                    val largestIcon = icons.maxByOrNull {
                        it.attr("sizes").split("x").let { sizeParts ->
                            if (sizeParts.size == 2) sizeParts[0].toIntOrNull() ?: 0 else 0
                        }
                    }

                    val iconUrl = largestIcon?.attr("href")
                    val absoluteIconUrl = if (iconUrl != null) resolveUrl(url, iconUrl) else null
                    callback(absoluteIconUrl)
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        return URL(URL(baseUrl), relativeUrl).toString()
    }

    private fun parseXmlToRssFeed(xml: String?): RssFeed? {
        if (xml == null) return null
        val serializer: Serializer = Persister()
        return try {
            serializer.read(RssFeed::class.java, StringReader(xml))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Root(name = "rss", strict = false)
class RssFeed @JvmOverloads constructor(

    @field:Element(name = "channel")
    var channel: Channel? = null
)

@Root(name = "channel", strict = false)
class Channel @JvmOverloads constructor(

    @field:Element(name = "title")
    var title: String = "",

    @field:ElementList(entry = "item", inline = true)
    var items: List<Item>? = null
)

@Root(name = "item", strict = false)
class Item @JvmOverloads constructor(

    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "description", required = false)
    var description: String? = null,

    @field:Element(name = "pubDate", required = false)
    var pubDate: String? = null
)