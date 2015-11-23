package com.expper.service.algorithms;

import java.util.Comparator;

/**
 * @author Raysmond<i@raysmond.com>
 */
public class HotComparator implements Comparator<PostMeta> {

    @Override
    public int compare(PostMeta o1, PostMeta o2) {
        return o2.hot > o1.hot ? 1 : (o2.hot < o1.hot ? -1 : 0);
    }

}
