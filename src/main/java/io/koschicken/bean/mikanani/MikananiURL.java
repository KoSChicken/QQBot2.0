package io.koschicken.bean.mikanani;

public interface MikananiURL {
    String BASE_URL = "https://mikanani.me";
    /// [搜索](https://mikanani.me/Home/Search?searchstr=%E5%BD%BB%E5%A4%9C%E4%B9%8B%E6%AD%8C&subgroupid=&page=1)
    String SEARCH_URL = "/Home/Search?searchstr=";
    /// [最近更新](https://mikanani.me/Home/EpisodeUpdateRows?predate=0&enddate=1&maximumitems=6)
    String DAY_UPDATE = "/Home/EpisodeUpdateRows";
    /// [更新列表](https://mikanani.me/Home/Classic/1)
    String LIST = "/Home/Classic";
    /// [字幕组信息](https://mikanani.me/Home/PublishGroup/12)
    String SUBGROUP = "/Home/PublishGroup";
    /// [番组信息](https://mikanani.me/Home/Bangumi/2748)
    String BANGUMI = "/Home/Bangumi";
}
