package cn.myerm.objectbuilder.service;

import java.util.List;
import java.util.Map;

public interface IJdbcTemplateService {
    Map<String, Object> tablelist();

    Map<String, Object> columnlist(String tablename);

    boolean isHaveTable(String table);

    void createDbTable(String table, Boolean isAuto);

    void createIndex(String table, String indexName, List<String> columnList);

    List<Map<String, Object>> isHaveColumn(String table,String column);

    void addColumn(String tableName,String columnName,String dataType,String uiType,Boolean isPk,Boolean isautoincrement);

    List<Map<String, Object>> columnlist(String tablename,String fieldlist);
}
