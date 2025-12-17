package com.example.plugin

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.jsoup.Jsoup
import java.net.URLEncoder

@WebDataSource(
    name = "Meionovels",
    provider = "meionovels.com"
)
class ExampleWebDataSource : WebBookDataSource {

    override val id: Int = "meionovels".hashCode()

    // SOURCE ONLINE
    override val offLine: Boolean = false
    override val isOffLineFlow: Flow<Boolean> = flowOf(false)
    override suspend fun isOffLine(): Boolean = false

    // 🚫 EXPLORE TIDAK DIGUNAKAN
    override val explorePageIdList: List<String> = emptyList()
    override val explorePageDataSourceMap = emptyMap<String, Any>()
    override val exploreExpandedPageDataSourceMap = emptyMap<String, Any>()

    // 🔍 SEARCH CONFIG
    override val searchTypeMap = mapOf("default" to "Search")
    override val searchTipMap = mapOf("default" to "Search Meionovels")
    override val searchTypeIdList = listOf("default")

    // =========================
    // 🔍 SEARCH IMPLEMENTATION
    // =========================
    override fun search(
        searchType: String,
        keyword: String
    ): Flow<List<BookInformation>> = flow {

        val encoded = URLEncoder.encode(keyword, "UTF-8")
        val url = "https://meionovels.com/?s=$encoded"

        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(15_000)
            .get()

        val result = mutableListOf<BookInformation>()

        // Meionovels search result selector
        val items = doc.select("h2.entry-title a")

        for (el in items) {
            val title = el.text().trim()
            val link = el.absUrl("href")

            if (title.isBlank() || link.isBlank()) continue

            val book = BookInformation.empty()
            book.id = link          // ID boleh URL
            book.title = title
            book.cover = ""         // nanti isi
            book.author = ""
            book.description = ""

            result.add(book)
        }

        // ❗ WAJIB ADA ISI → JANGAN EMIT LIST KOSONG
        if (result.isEmpty()) {
            result.add(BookInformation.empty())
        }

        emit(result)
    }

    // =========================
    // ❌ BELUM DIPAKAI
    // =========================
    override suspend fun getBookInformation(id: String): BookInformation {
        return BookInformation.empty()
    }

    override suspend fun getBookVolumes(id: String): BookVolumes {
        return BookVolumes.empty()
    }

    override suspend fun getChapterContent(
        chapterId: String,
        bookId: String
    ): ChapterContent {
        return ChapterContent.empty()
    }

    override fun stopAllSearch() {}
}
