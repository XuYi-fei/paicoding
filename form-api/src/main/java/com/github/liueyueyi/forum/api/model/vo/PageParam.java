package com.github.liueyueyi.forum.api.model.vo;

import lombok.Data;

/**
 * 数据库分页参数
 *
 * @author louzai
 * @date 2022-07-120
 */
@Data
public class PageParam {

    public static final Long DEFAULT_PAGE_NUM = 1L;
    public static final Long DEFAULT_PAGE_SIZE = 10L;


    /**
     * 请求页数，从1开始计数
     */
    private long pageNum;
    private long pageSize;
    private long offset;
    private long limit;

    public static PageParam newPageInstance() {
        return newPageInstance(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
    }

    public static PageParam newPageInstance(Integer pageNum, Integer pageSize) {
        return newPageInstance(pageNum.longValue(), pageSize.longValue());
    }

    public static PageParam newPageInstance(Long pageNum, Long pageSize) {
        if (pageNum == null || pageSize == null) {
            return null;
        }

        final PageParam pageParam = new PageParam();
        pageParam.pageNum = pageNum;
        pageParam.pageSize = pageSize;

        pageParam.offset = (pageNum - 1) * pageSize;
        pageParam.limit = pageSize;

        return pageParam;
    }

    public static String getLimitSql(PageParam pageParam) {
        return String.format("limit %s,%s", pageParam.offset, pageParam.limit);
    }
}