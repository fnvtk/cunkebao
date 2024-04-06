package cn.myerm.business.param;

import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class ListParam {
    /**
     * 页码
     */
    private int page;

    /**
     * 每页记录数
     */
    private int pagelimit;

    /**
     * 视图id
     */
    @Min(value = 0)
    private int listid;

    /**
     * 是否可以分页
     */
    private Integer canpage;

    /**
     * 自定义显示列
     */
    private List<String> dispcol;

    /**
     * 分类tab的id
     */
    private String tabid;

    /**
     * 相关信息的id
     */
    private String relatedid;

    /**
     * 相关信息的对象数据ID
     */
    private String objectid;

    /**
     * 快速搜索项的字段名
     */
    private String fastsearchfield;

    /**
     * 快速搜索的搜索词
     */
    private String fastsearchkeyword;

    /**
     * 批量选择的id
     */
    private String selectedids;

    /**
     * 高级搜索
     */
    private String advsearchjson;

    /**
     * 对象名
     */
    private String sobjectname;

    /**
     * 排序
     */
    private String orderby;

    /**
     * 针对对象名称的搜索关键词
     */
    private String keyword;
}
