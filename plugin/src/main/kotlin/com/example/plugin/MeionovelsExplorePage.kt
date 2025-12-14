object MeionovelsExplorePage : ExplorePageDataSource {

    override val title: String
        get() = "Meionovels"

    override fun getExplorePage(): ExplorePage {
        return ExplorePage(
            sections = listOf(
                ExplorePageSection(
                    title = "Placeholder",
                    books = emptyList()
                )
            )
        )
    }
}