package cn.myerm.alertcenter.alert;

import lombok.Data;

@Data
public class Message {
    /**
     * 应用名称
     */
    private String appname;

    /**
     * 端名
     */
    private String end;

    /**
     * 主机名
     */
    private String host;

    /**
     * 故障定位
     */
    private String position;

    /**
     * 故障摘要
     */
    private String msg;

    /**
     * 故障详情
     */
    private String detail;

    /**
     * 故障级别
     */
    private int level;

    /**
     * 故障级别
     */
    private String type;

    /**
     * 发生时间
     */
    private String happentime;

    /**
     * 保存时间
     */
    private String savetime;

    /**
     * 环境信息
     */
    private String env;

    /**
     * 原始信息
     */
    private String raw;

    /**
     * Md5
     */
    private String md5;
}
