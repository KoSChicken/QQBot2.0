package io.koschicken.bean.mikanani;

import io.koschicken.bean.Magnet;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class Mikanani {

    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 SE 2.X MetaSr 1.0";
    private final static int MAX = 5;
    private final static int MAX_EP = 5;

    private String name;
    private List<Magnet> mags;

    public static List<Mikanani> search(String keyword) throws IOException {
        Document document = Jsoup.connect(MikananiURL.BASE_URL + MikananiURL.SEARCH_URL + keyword).get();
        Element leftBar = document.select(".pull-left.leftbar-container").get(0);
        Elements subGroups = leftBar.select(".leftbar-item");
        List<String> groupIds = new ArrayList<>();
        subGroups.forEach(element -> {
            String groupId = element.select(".subgroup-longname").get(0).attr("data-subgroupid");
            groupIds.add(groupId);
        });
        List<Mikanani> list = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX, groupIds.size()); i++) {
            String id = groupIds.get(i);
            try {
                if (StringUtils.hasText(id)) {
                    list.add(search(keyword, id));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static Mikanani search(String keyword, String subGroupId) throws IOException {
        Mikanani mikanani = new Mikanani();
        mikanani.setName(keyword);
        Document document = Jsoup.connect(MikananiURL.BASE_URL + MikananiURL.SEARCH_URL + keyword + "&subgroupid=" + subGroupId).get();
        Elements items = document.select(".js-search-results-row");
        List<Magnet> mags = new ArrayList<>();
        for (int j = 0; j < Math.min(MAX_EP, items.size()); j++) {
            Element i = items.get(j);
            Elements children = i.children();
            Magnet magnet = new Magnet();
            String name = children.get(0).text();
            magnet.setName(name.substring(0, name.lastIndexOf('[')));
            magnet.setSize(children.get(1).text());
            magnet.setReleaseTime(children.get(2).text());
            magnet.setMag(children.get(0).select(".js-magnet.magnet-link").attr("data-clipboard-text"));
            mags.add(magnet);
        }
        mikanani.setMags(mags);
        return mikanani;
    }

    public static void main(String[] args) throws IOException {
        List<Mikanani> list = search("彻夜之歌");
        list.forEach(System.out::println);
    }
}
