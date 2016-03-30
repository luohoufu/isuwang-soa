package com.isuwang.soa.core.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version
 *
 * @author craneding
 * @date 16/3/24
 */
public class Version {
    private static final Pattern versionPattern = Pattern.compile("(\\d+)(\\.(\\d+))?(\\.(\\d+))?(.*)");

    private final String fullName;
    private final Integer majorNum;
    private final Integer minorNum;
    private final Integer patchNum;

    private Version(String fullName, Integer majorNum, Integer minorNum, Integer patchNum) {
        this.fullName = fullName;
        this.majorNum = majorNum;
        this.minorNum = minorNum;
        this.patchNum = patchNum;
    }

    public boolean compatibleTo(Version required) {
        if (required.majorNum != this.majorNum) return false;

        if (required.minorNum > this.minorNum) return false;

        if (required.minorNum == this.minorNum) return required.patchNum > this.patchNum;

        return true;
    }

    public static Version toVersion(String fullName) {
        Matcher matcher = versionPattern.matcher(fullName);

        if (!matcher.matches())
            throw new IllegalArgumentException("版本格式错误:" + fullName);

        String majorName = matcher.group(1);
        String minorName = matcher.group(3);
        String patchName = matcher.group(5);
        //String others = matcher.group(6);

        if (minorName == null || minorName.trim().isEmpty())
            minorName = "0";
        if (patchName == null || patchName.trim().isEmpty())
            patchName = "0";

        return new Version(fullName, Integer.parseInt(majorName), Integer.parseInt(minorName), Integer.parseInt(patchName));
    }

    @Override
    public String toString() {
        return fullName;
    }
    
}
