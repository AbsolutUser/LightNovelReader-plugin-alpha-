package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
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
 * Meionovels datasource implementation.
 *
 * - Uses Jsoup to fetch and parse HTML.
 * - Caches mappings between string IDs (we use the URL as ID) and real URLs.
 * - Parses title, cover, author, summary, chapter list and chapter content.
 *
 * NOTE: if your LNReader API has slightly different model constructors,
 * the compiler may complain. If that happens, paste the compile error here
 * and aku perbaiki langsung.
 */

@WebDataSource(
    name = "MeioNovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    private val base = "https://meionovels.com"

    // caches keyed by String (we use the absolute URL as stable ID)
    private val bookUrlMap = mutableMapOf<String, String>()
    private val chapterUrlMap = mutableMapOf<String, String>()
    private val bookChaptersMap = mutableMapOf<String, List<String>>()
    private val cacheMutex = Mutex()

    override val id: Int
        get() = "meionovels".hashCode()

    override val offLine: Boolean
        get() = false

    override val isOffLineFlow: Flow<Boolean>
        get() = flowOf(false)

    // No explore pages (prevents Explore crashes)
    override val explorePageIdList: List<String>
        get() = emptyList()
    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource>
        get() = emptyMap()
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>
        get() = emptyMap()

    override val searchTypeMap: Map<String, String>
        get() = mapOf("all" to "All")
    override val searchTipMap: Map<String, String>
        get() = mapOf("all" to "Use this to search titles on MeioNovels")
    override val searchTypeIdList: List<String>
        get() = listOf("all")

    override suspend fun isOffLine(): Boolean {
        return false
    }

    // --- Helpers ---

    private fun fetchDoc(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android) MeionovelsPlugin/0.2")
            .timeout(20_000)
            .get()
    }

    private fun urlToId(url: String): String {
        // Use the absolute URL as ID for simplicity
        return url
    }

    // ---------- Implementations ----------

    override suspend fun getBookInformation(id: String): BookInformation {
        val bookUrl = cacheMutex.withLock { bookUrlMap[id] } ?: return BookInformation.Companion.empty()
        return try {
            val doc = fetchDoc(bookUrl)

            val title = doc.selectFirst("h1.entry-title")?.text()?.trim() ?: doc.title()
            val cover = doc.selectFirst(".post-thumbnail img, .entry-content img")?.absUrl("src") ?: ""
            val author = doc.selectFirst(".author, .entry-meta a[rel=author]")?.text() ?: ""
            val description = doc.selectFirst(".entry-content > p")?.text()
                ?: doc.selectFirst(".entry-summary")?.text() ?: ""

            // Build BookInformation: start from empty and copy fields
            val empty = BookInformation.Companion.empty()
            // try to populate using copy (works if the implementation is a data class)
            try {
                return empty.copy(
                    id = id,
                    title = title,
                    cover = cover,
                    author = author,
                    description = description
                )
            } catch (_: Throwable) {
                // fallback: return empty if copying not supported
                return empty
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return BookInformation.Companion.empty()
        }
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        val bookUrl = cacheMutex.withLock { bookUrlMap[id] } ?: return BookVolumes.Companion.empty()
        return try {
            val doc = fetchDoc(bookUrl)

            // Common selectors for chapter links on meionovels
            val elems = doc.select(".entry-content a[href*=\"chapter\"], .chapter-list a, .post a[href*=\"/chapter\"], a[href*=\"/volume-\"]")
            val chapters = mutableListOf<Pair<String, String>>()
            for (el in elems) {
                val href = el.absUrl("href")
                val text = el.text().trim()
                if (href.isNotBlank() && text.isNotBlank()) {
                    chapters.add(text to href)
                }
            }

            // fallback selectors
            if (chapters.isEmpty()) {
                val els2 = doc.select("a[href*=\"/read/\"]")
                for (el in els2) {
                    val href = el.absUrl("href")
                    val text = el.text().trim()
                    if (href.isNotBlank() && text.isNotBlank()) chapters.add(text to href)
                }
            }

            // create chapterIds and cache
            val chapterIds = chapters.map { (_, url) ->
                val cid = urlToId(url)
                cacheMutex.withLock { chapterUrlMap[cid] = url }
                cid
            }

            cacheMutex.withLock {
                bookChaptersMap[id] = chapterIds
            }

            // Build a simple single-volume BookVolumes
            val volumeTitle = "Chapters"
            try {
                // Prefer building via copy on empty instance
                val emptyVolumes = BookVolumes.Companion.empty()
                // Try to create Volume objects if available
                val volList = listOf(
                    Volume(
                        volumeId = "v1",
                        volumeTitle = volumeTitle,
                        chapters = chapterIds
                    )
                )
                // Try to copy volumes into BookVolumes
                return emptyVolumes.copy(volumes = volList)
            } catch (_: Throwable) {
                // Fallback: try copying fields if simple copy available
                try {
                    val emptyVolumes = BookVolumes.Companion.empty()
                    return emptyVolumes.copy(
                        // if BookVolumes has a 'volumes' parameter
                        volumes = listOf()
                    )
                } catch (_: Throwable) {
                    // final fallback
                    return BookVolumes.Companion.empty()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return BookVolumes.Companion.empty()
        }
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        val url = cacheMutex.withLock { chapterUrlMap[chapterId] } ?: run {
            // try lookup via bookChaptersMap
            val list = cacheMutex.withLock { bookChaptersMap[bookId] } ?: emptyList()
            if (chapterId in list) {
                cacheMutex.withLock { chapterUrlMap[chapterId] } ?: return ChapterContent.Companion.empty()
            } else {
                return ChapterContent.Companion.empty()
            }
        }

        return try {
            val doc = fetchDoc(url)
            val title = doc.selectFirst("h1.entry-title")?.text()?.trim() ?: ""
            val contentEl = doc.selectFirst(".entry-content, .chapter-content, .post-content, .read-content") ?: doc.body()
            contentEl.select("script, style, .nav, .pagination, .ads, .share, .advertisement").remove()
            val paragraphs = contentEl.select("p").map { it.text().trim() }.filter { it.isNotBlank() }
            val contentText = paragraphs.joinToString("\n\n")

            val empty = ChapterContent.Companion.empty()
            try {
                return empty.copy(
                    id = chapterId,
                    title = title,
                    content = contentText
                )
            } catch (_: Throwable) {
                return empty
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ChapterContent.Companion.empty()
        }
    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        val url = "$base/?s=$q"
        try {
            val doc = fetchDoc(url)
            val results = mutableListOf<BookInformation>()

            val items = doc.select("article h2.entry-title a, .post .entry-title a, .result-item a, .search-results a")
            for (el in items) {
                val title = el.text().trim()
                val href = el.absUrl("href")
                if (href.isNotBlank()) {
                    val bid = urlToId(href)
                    cacheMutex.withLock { bookUrlMap[bid] = href }

                    val cover = try {
                        // try to get an image near the item
                        el.ownerDocument().selectFirst(".post-thumbnail img")?.absUrl("src") ?: ""
                    } catch (_: Throwable) {
                        ""
                    }

                    val empty = BookInformation.Companion.empty()
                    try {
                        val bi = empty.copy(
                            id = bid,
                            title = title,
                            cover = cover,
                            author = ""
                        )
                        results.add(bi)
                    } catch (_: Throwable) {
                        // fallback add empty to indicate end / skip
                        results.add(BookInformation.Companion.empty())
                    }
                }
            }

            // emit results (end of stream signaled by adding empty())
            emit(results)
            emit(listOf(BookInformation.Companion.empty()))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(listOf(BookInformation.Companion.empty()))
        }
    }

    override fun stopAllSearch() {
        // no-op
    }
}