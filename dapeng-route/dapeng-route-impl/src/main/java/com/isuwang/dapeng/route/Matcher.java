package com.isuwang.dapeng.route;

import com.isuwang.dapeng.route.pattern.Pattern;

import java.util.List;

/**
 * Created by tangliu on 2016/6/19.
 */
public class Matcher {

    public Id id;

    public MatcherId matcherID;

    public List<Pattern> patterns;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public MatcherId getMatcherID() {
        return matcherID;
    }

    public void setMatcherID(MatcherId matcherID) {
        this.matcherID = matcherID;
    }
}
