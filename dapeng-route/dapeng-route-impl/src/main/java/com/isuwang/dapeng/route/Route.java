package com.isuwang.dapeng.route;

import com.isuwang.dapeng.route.pattern.Pattern;

/**
 * Created by tangliu on 2016/6/19.
 */
public class Route {

    public MatchLeftSide left;

    public Pattern right;

    public MatchLeftSide getLeft() {
        return left;
    }

    public void setLeft(MatchLeftSide left) {
        this.left = left;
    }

    public void setRight(Pattern right) {
        this.right = right;
    }
}
