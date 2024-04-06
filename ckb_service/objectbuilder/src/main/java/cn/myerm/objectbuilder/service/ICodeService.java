package cn.myerm.objectbuilder.service;

public interface ICodeService {
    boolean create(String tableName,String saveUrl,String basePackageUrl,String objectname,String moduleid) throws Exception;
    boolean createlist(String sobjectname);
}
