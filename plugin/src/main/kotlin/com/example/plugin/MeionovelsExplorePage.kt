package com.example.plugin

import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import io.nightfish.lightnovelreader.api.web.explore.model.ExplorePage

/**
 * Minimal ExplorePageDataSource
 * Compatible with LightNovelReader refactoring-SNAPSHOT API
 */
object MeionovelsExplorePage : ExplorePageDataSource {

    override val title: String
        get() = "Meionovels"

    override fun getExplorePage(): ExplorePage {
        // SAFEST implementation
        // Prevents Explore tab crash
        return ExplorePage.empty()
    }
}