export default {
    id: "meionovels",
    name: "Meionovels",
    site: "https://meionovels.com",
    version: "0.1.0",

    // ===== SEARCH =====
    async search(keyword, page) {
        const url = `${this.site}/?s=${encodeURIComponent(keyword)}`;
        const doc = await fetch(url).then(r => r.text()).then(html => parseHtml(html));

        const list = [];
        doc.select("article h2.entry-title a").forEach(a => {
            list.push({
                id: a.attr("href"),
                title: a.text(),
                cover: "",
                author: ""
            });
        });

        return {
            list,
            hasNextPage: false
        };
    },

    // ===== BOOK DETAIL =====
    async detail(id) {
        const doc = await fetch(id).then(r => r.text()).then(html => parseHtml(html));

        return {
            id,
            title: doc.select("h1.entry-title").text(),
            author: "",
            cover: doc.select(".post-thumbnail img").attr("src"),
            description: doc.select(".entry-content p").first()?.text() ?? "",
            chapters: this._parseChapters(doc)
        };
    },

    // ===== CHAPTER CONTENT =====
    async chapter(chapter) {
        const doc = await fetch(chapter.url)
            .then(r => r.text())
            .then(html => parseHtml(html));

        doc.select("script, style, .ads").remove();

        return {
            title: chapter.title,
            content: doc.select(".entry-content").html()
        };
    },

    // ===== INTERNAL =====
    _parseChapters(doc) {
        const chapters = [];
        doc.select(".entry-content a[href*='chapter']").forEach(a => {
            chapters.unshift({
                title: a.text(),
                url: a.attr("href")
            });
        });
        return chapters;
    }
};
