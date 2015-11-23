package com.expper.service.algorithms;

import org.joda.time.DateTime;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class HotTest {
    private static List<PostMeta> posts = new ArrayList<>();

    public static void initialize(long size) {
        for (long i = 0; i < size; ++i) {
            PostMeta post = new PostMeta();
            post.id = i;
            post.ups = (int) (Math.random() * 10000);
            post.downs = (int) (Math.random() * 10000);
            ZonedDateTime date = ZonedDateTime.now();
            date = date.minusHours((int) (Math.random() * 24 * 30 * 6));
            date = date.minusSeconds((int) (Math.random() * 3600));
            post.createdAt = date;
            post.hot = (int) (Math.random() * 2000);

            posts.add(post);
        }
    }

    public static void runAlgorithm(String algorithm) throws Exception {
        long start = System.currentTimeMillis();

        if (algorithm.equals("hackernews")) {
            HackNews hknAlgorithm = new HackNews(ZonedDateTime.now(), 1.5);
            hknAlgorithm.updateHots(posts);
            hknAlgorithm.sort(posts);

        } else if (algorithm.equals("reddit")) {
            Reddit reddit = new Reddit();
            reddit.updateHots(posts);
            reddit.sort(posts);
        } else {
            throw new Exception("Unsupported algorithm " + algorithm);
        }

        long end = System.currentTimeMillis();
        System.out.println("\nTotal time: " + (end - start) + " ms");

//        long totalMemory = MemoryMeasurer.measureBytes(posts);
//        System.out.println("Total memory: " + totalMemory/(1024*1024) + " MB");
//        System.out.println("Average entry memory: " + totalMemory/posts.size() + " bytes");

        System.out.println("Top 100: ");
        for (int i = 0; i < 100; i++) {
            System.out.println(posts.get(i).toString());
        }
    }


    public static void main(String[] args) throws Exception {
        runAlgorithm("hackernews");

        List<PostMeta> top100 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            top100.add(posts.get(i));
        }

        runAlgorithm("reddit");

        System.out.println("\n\nCommon posts: ");
        int count = 0;
        for (int i = 0; i < 100; i++) {
            if (top100.contains(posts.get(i))) {
                System.out.println("" + (++count) + ": " + posts.get(i).toString());
            }
        }
    }
}
