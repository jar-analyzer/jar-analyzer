package me.n1ar4.jar.analyzer.engine.Index.entity;

import java.util.List;
import java.util.Map;

public class Result {
    // 结果总命中数
    private Long total;
    // 返回命中数据
    private List<Map<String, Object>> data;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

}
