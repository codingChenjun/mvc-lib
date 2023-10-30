package com.nf.mvc.support.path;

import com.nf.mvc.support.PathMatcher;

public class EqualPathMatcher implements PathMatcher {
    @Override
    public boolean isMatch(String pattern, String path) {
        return path.equals(pattern);
    }
}
