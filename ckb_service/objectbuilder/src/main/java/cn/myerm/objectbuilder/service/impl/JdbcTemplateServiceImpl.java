package cn.myerm.objectbuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.myerm.common.config.Businessconfig;
import cn.myerm.objectbuilder.service.IJdbcTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JdbcTemplateServiceImpl implements IJdbcTemplateService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Resource
    Businessconfig businessconfig;

    @Override
    public Map<String, Object> tablelist() {
        String sql = "select table_name from information_schema.tables where table_schema=?";

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, businessconfig.getDbname());

        List<String> tableNameList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            tableNameList.add((String) map.get("table_name"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dbtables", tableNameList);
        return result;
    }

    public boolean isHaveTable(String table) {
        String sql = "select table_name from information_schema.tables where table_schema=? and table_type=? and table_name=?";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, businessconfig.getDbname(), "base table", table);
        if (CollectionUtil.isNotEmpty(mapList)) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> columnlist(String tablename) {
        String sql = "select column_name from information_schema.columns where table_schema=? and table_name=?";

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, businessconfig.getDbname(), tablename);

        List<String> dbColumnList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            dbColumnList.add((String) map.get("column_name"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("columns", dbColumnList);
        return result;
    }


    /**
     * 建表并添加lID和sName字段
     *
     * @param table
     * @param isAuto
     */
    public void createDbTable(String table, Boolean isAuto) {
        String sql = "CREATE TABLE ";
        sql += table;
        sql += "(";
        sql += "lID INT PRIMARY KEY ";
        if (isAuto) {
            sql += " AUTO_INCREMENT ";
        }
        sql += " ) ENGINE = InnoDB DEFAULT CHARSET=utf8;";

        jdbcTemplate.execute(sql);
    }

    /**
     * 新建索引
     *
     * @param table
     * @param indexName
     * @param columnList
     */
    public void createIndex(String table, String indexName, List<String> columnList) {
        String sql = "ALTER TABLE ";
        sql += table;
        sql += " ADD INDEX ";
        sql += indexName;
        sql += " ( ";
        String str = String.join(",", columnList);
        sql += str;
        sql += " ) ";

        jdbcTemplate.execute(sql);
    }

    @Override
    public List<Map<String, Object>> isHaveColumn(String table, String column) {
        String existsql = "select column_name from information_schema.columns where table_schema=? and table_name=? and column_name =?";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(existsql, businessconfig.getDbname(), table, column);
        return mapList;
    }

    @Override
    public void addColumn(String tableName, String columnName, String dataType, String uiType, Boolean isPk, Boolean isautoincrement) {
        String sql = "ALTER TABLE " + tableName + " ADD " + columnName + " " + dataType;

        if (dataType.contains("int") || dataType.contains("tinyint") || dataType.contains("decimal")) {
            if (isPk == null || !isPk) {
                if (!uiType.equals("ListTable")) {
                    sql += " DEFAULT 0";
                }
            }
        }

        if (ObjectUtil.isNotEmpty(isPk)) {
            sql += " PRIMARY KEY ";

            if (ObjectUtil.isNotEmpty(isautoincrement)) {
                sql += " AUTO_INCREMENT ";
            }
        }
        jdbcTemplate.execute(sql);
    }

    @Override
    public List<Map<String, Object>> columnlist(String tablename, String fieldlist) {
        String sql = "select column_name from information_schema.columns where table_schema=? and table_name=? and column_name not in(?)";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, businessconfig.getDbname(), tablename, fieldlist);
        return mapList;
    }
}
