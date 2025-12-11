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

@WebDataSource(
    name = "MeioNovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    private val base = "https://meionovels.com"

    // caches keyed by String (API expects String ids)
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

    private fun fetchDoc(url: String): Document {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android) MeionovelsPlugin/0.1")
            .timeout(15_000)
            .get()
    }

    private fun urlToId(url: String): String {
        // use the URL itself as ID (simple and stable)
        return url
    }

    // ---------- Minimal implementations (empty objects to satisfy API) ----------

    override suspend fun getBookInformation(id: String): BookInformation {
        val bookUrl = cacheMutex.withLock { bookUrlMap[id] } ?: return BookInformation.Companion.empty()
        // parse if needed (we keep minimal for now)
        val doc = fetchDoc(bookUrl)
        // TODO: build real BookInformation using API's builder / factory
        return BookInformation.Companion.empty()
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        val bookUrl = cacheMutex.withLock { bookUrlMap[id] } ?: return BookVolumes.Companion.empty()
        val doc = fetchDoc(bookUrl)

        // Try to collect chapter URLs and cache them
        val elems = doc.select(".entry-content a[href*=\"chapter\"], .chapter-list a, .post a[href*=\"/chapter\"]")
        val chapters = mutableListOf<Pair<String, String>>()
        for (el in elems) {
            val href = el.absUrl("href")
            val text = el.text().trim()
            if (href.isNotBlank() && text.isNotBlank()) chapters.add(text to href)
        }

        if (chapters.isEmpty()) {
            val els2 = doc.select("a[href*=\"/read/\"]")
            for (el in els2) {
                val href = el.absUrl("href")
                val text = el.text().trim()
                if (href.isNotBlank() && text.isNotBlank()) chapters.add(text to href)
            }
        }

        val chapterIds = chapters.map { (_, url) ->
            val cid = urlToId(url)
            cacheMutex.withLock { chapterUrlMap[cid] = url }
            cid
        }

        cacheMutex.withLock {
            bookChaptersMap[id] = chapterIds
        }

        // return empty BookVolumes for now to ensure compilation
        return BookVolumes.Companion.empty()
    }

    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        val url = cacheMutex.withLock { chapterUrlMap[chapterId] } ?: return ChapterContent.Companion.empty()
        val doc = fetchDoc(url)
        val contentEl = doc.selectFirst(".entry-content, .chapter-content, .post-content, .read-content") ?: doc.body()
        contentEl.select("script, style, .nav, .pagination, .ads, .share, .advertisement").remove()
        val paragraphs = contentEl.select("p").map { it.text().trim() }.filter { it.isNotBlank() }
        val contentText = paragraphs.joinToString("\n\n")

        // TODO: build real ChapterContent object using API; return empty for now
        return ChapterContent.Companion.empty()
    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow {
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
                // add empty BookInformation for now
                results.add(BookInformation.Companion.empty())
            }
        }
        emit(results)
    }

    override fun stopAllSearch() {
        // no-op for now
    }
}