package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

/**
 * Simple Meionovels datasource implementation.
 *
 * NOTE:
 * - This implementation uses Jsoup for HTML fetch+parsing.
 * - It keeps a minimal in-memory cache (map) to link integer IDs (bookId/chapterId)
 *   to real URLs. This is simplest for the LNReader API where functions use Int ids.
 *
 * If your template provides a different http/jsoup util in utils (e.g. MixDataCache or a http client),
 * you can replace fetchDoc(...) with those utilities for better integration.
 */

@WebDataSource(
    name = "MeioNovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    // base site
    private val base = "https://meionovels.com"

    // Simple in-memory caches to map Int id -> URL
    // bookId -> bookUrl
    private val bookUrlMap = mutableMapOf<Int, String>()
    // chapterId -> chapterUrl
    private val chapterUrlMap = mutableMapOf<Int, String>()
    // mapping of bookId -> list of chapterIds (order preserved)
    private val bookChaptersMap = mutableMapOf<Int, List<Int>>()
    // mutex to protect maps in concurrent env
    private val cacheMutex = Mutex()

    override val id: Int
        get() = "meionovels".hashCode()

    override val offLine: Boolean
        get() = false

    override val isOffLineFlow: Flow<Boolean>
        get() = flowOf(false)

    // minimal: no explore pages implemented
    override val explorePageIdList: List<String>
        get() = emptyList()
    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource>
        get() = emptyMap()
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>
        get() = emptyMap()

    // search types: just "all"
    override val searchTypeMap: Map<String, String>
        get() = mapOf("all" to "All")
    override val searchTipMap: Map<String, String>
        get() = mapOf("all" to "Use this to search titles on MeioNovels")
    override val searchTypeIdList: List<String>
        get() = listOf("all")

    override suspend fun isOffLine(): Boolean {
        return false
    }

    // Helper: fetch Document using Jsoup (synchronous). Jsoup will follow redirects.
    private fun fetchDoc(url: String): Document {
        // add simple UA, timeout. If your utils have UserAgentsGenerator, swap here.
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android) MeionovelsPlugin/0.1")
            .timeout(15_000)
            .get()
    }

    // Helper: make int id from URL (stable within session)
    private fun urlToId(url: String): Int {
        return url.hashCode()
    }

    // ---------- Core functions to implement ----------

    override suspend fun getBookInformation(id: Int): BookInformation {
        // find bookUrl
        val bookUrl = cacheMutex.withLock { bookUrlMap[id] }
            ?: throw IllegalArgumentException("Unknown book id: $id")

        val doc = fetchDoc(bookUrl)

        // parse title, cover, author, description
        val title = doc.selectFirst("h1.entry-title")?.text()?.trim() ?: doc.title()
        val cover = doc.selectFirst(".post-thumbnail img, .entry-content img")?.absUrl("src") ?: ""
        val author = doc.selectFirst(".author, .entry-meta a[rel=author]")?.text() ?: ""
        val description = doc.selectFirst(".entry-content > p")?.text() ?: doc.selectFirst(".entry-summary")?.text() ?: ""

        // Build BookInformation object
        // NOTE: The exact constructor/properties of BookInformation depend on the LNReader API version.
        // If the below constructor does not match, open the BookInformation class in the template and adapt fields accordingly.
        return BookInformation(
            id = id,
            title = title,
            cover = cover,
            author = author,
            description = description
        )
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes {
        // For simplicity: parse chapter list from book page and return as single volume
        val bookUrl = cacheMutex.withLock { bookUrlMap[id] } ?: throw IllegalArgumentException("Unknown book id: $id")
        val doc = fetchDoc(bookUrl)

        // Try selectors that commonly contain chapters
        val elems = doc.select(".entry-content a[href*=\"chapter\"], .chapter-list a, .post a[href*=\"/chapter\"]")
        val chapters = mutableListOf<Pair<String, String>>() // pair<title, url>
        for (el in elems) {
            val href = el.absUrl("href")
            val text = el.text().trim()
            if (href.isNotBlank() && text.isNotBlank()) chapters.add(text to href)
        }

        // fallback: sometimes chapter links are under list items
        if (chapters.isEmpty()) {
            val els2 = doc.select("a[href*=\"/read/\"]")
            for (el in els2) {
                val href = el.absUrl("href")
                val text = el.text().trim()
                if (href.isNotBlank() && text.isNotBlank()) chapters.add(text to href)
            }
        }

        // create ids for chapters and store in cache
        val chapterIds = chapters.map { (_, url) ->
            val cid = urlToId(url)
            cacheMutex.withLock { chapterUrlMap[cid] = url }
            cid
        }

        cacheMutex.withLock {
            bookChaptersMap[id] = chapterIds
        }

        // Build BookVolumes instance.
        // NOTE: BookVolumes structure depends on LNReader API. A common simple structure:
        // BookVolumes(volumes = listOf(Volume(title, chaptersList)))
        // We'll build a minimal example; adapt if constructor differs.
        val volume = BookVolumes(
            // try to fill common fields. If your BookVolumes requires other types, open its class and adapt.
            volumes = listOf(
                io.nightfish.lightnovelreader.api.book.Volume(
                    title = "Chapters",
                    chapterIds = chapterIds
                )
            )
        )
        return volume
    }

    override suspend fun getChapterContent(
        chapterId: Int,
        bookId: Int
    ): ChapterContent {
        // find chapter url
        val url = cacheMutex.withLock { chapterUrlMap[chapterId] } ?: run {
            // maybe chapterId is index-based: try to lookup by bookId -> chapters list
            val list = cacheMutex.withLock { bookChaptersMap[bookId] } ?: emptyList()
            if (chapterId in list) {
                cacheMutex.withLock { chapterUrlMap[chapterId] } ?: throw IllegalArgumentException("Unknown chapter id")
            } else {
                throw IllegalArgumentException("Unknown chapter id: $chapterId")
            }
        }

        val doc = fetchDoc(url)
        val contentEl = doc.selectFirst(".entry-content, .chapter-content, .post-content, .read-content") ?: doc.body()
        // remove junk
        contentEl.select("script, style, .nav, .pagination, .ads, .share, .advertisement").remove()

        val paragraphs = contentEl.select("p").map { it.text().trim() }.filter { it.isNotBlank() }
        val contentText = paragraphs.joinToString("\n\n")

        // Build ChapterContent object (adapt if constructor differs)
        return ChapterContent(
            id = chapterId,
            title = doc.selectFirst("h1.entry-title")?.text()?.trim() ?: "",
            content = contentText
        )
    }

    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> = flow {
        // Use site search: ?s=keyword
        val q = URLEncoder.encode(keyword, "utf-8")
        val url = "$base/?s=$q"
        val doc = fetchDoc(url)

        val results = mutableListOf<BookInformation>()
        val items = doc.select("article h2.entry-title a, .post .entry-title a, .result-item a")
        for (el in items) {
            val title = el.text().trim()
            val href = el.absUrl("href")
            if (href.isNotBlank()) {
                val bid = urlToId(href)
                cacheMutex.withLock { bookUrlMap[bid] = href }
                // Minimal BookInformation; adapt fields per actual class
                val bi = BookInformation(
                    id = bid,
                    title = title,
                    cover = doc.selectFirst(".post-thumbnail img")?.absUrl("src") ?: "",
                    author = ""
                )
                results.add(bi)
            }
        }
        emit(results)
    }

    override fun stopAllSearch() {
        // Not implementing advanced cancellation; template may provide search jobs to cancel.
        // Leave empty for now.
    }
}