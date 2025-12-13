package com.example.plugin

import io.nightfish.lightnovelreader.api.web.explore.ExplorePage
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource

/**
 * Minimal ExplorePageDataSource
 * Required so Explore tab does not crash
 */
object MeionovelsExplorePage : ExplorePageDataSource {

    override val title: String
        get() = "Meionovels"

    override fun getExplorePage(): ExplorePage {
        // SAFEST possible implementation
        // Never crashes, guaranteed valid
        return ExplorePage.empty()
    }
}