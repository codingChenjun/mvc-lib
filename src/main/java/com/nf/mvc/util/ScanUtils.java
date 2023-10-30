package com.nf.mvc.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public interface ScanUtils {

    /**
     * 指定同样的包或者有包含的关系的包时，类是不会被重复扫描到结果中的
     *
     * @param packageNames 要扫描的包
     * @return ObjectGraph扫描结果
     */
    static ScanResult scan(String... packageNames) {
        ClassGraph classGraph = new ClassGraph();
        classGraph.enableAllInfo();
        classGraph.acceptPackages(packageNames);
        return classGraph.scan();
    }
}
