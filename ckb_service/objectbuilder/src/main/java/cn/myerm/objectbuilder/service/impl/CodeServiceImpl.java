package cn.myerm.objectbuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.common.exception.SystemException;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.service.ICodeService;
import cn.myerm.objectbuilder.service.ISysObjectService;
import cn.myerm.objectbuilder.utils.FreeMarkerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class CodeServiceImpl implements ICodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeServiceImpl.class);

    @Autowired
    FreeMarkerUtils freeMarkerUtil;

    @Autowired
    ISysObjectService sysObjectService;

    @Override
    public boolean create(String tableName, String saveUrl, String basePackageUrl, String objectname, String moduleid) throws Exception {
        String projectPath = System.getProperty("user.dir");
        String codePath = projectPath + File.separator + "business" + File.separator + "src" + File.separator + "main";

        saveUrl = StrUtil.isEmpty(saveUrl) ? codePath : saveUrl;
        basePackageUrl = StrUtil.isEmpty(basePackageUrl) ? "cn.myerm" : basePackageUrl;
        //bean类名
        String entityName = tableName;

        //封装参数
        Map<String, Object> root = new HashMap<>();
        root.put("basePackageUrl", basePackageUrl);
        root.put("moduleId", moduleid);
        root.put("tableName", tableName);
        root.put("objecName", objectname);
        root.put("objecNameLower", objectname.toLowerCase());
        root.put("entityName", entityName);
        root.put("entityNameLower", freeMarkerUtil.getEntityNameLower(tableName));
        root.put("columns", freeMarkerUtil.getDataInfo(tableName));

        LOGGER.info(saveUrl);

        freeMarkerUtil.generate(root, "entity.ftl", saveUrl, entityName + ".java");
        freeMarkerUtil.generate(root, "controller.ftl", saveUrl, entityName + "Controller.java");
        freeMarkerUtil.generate(root, "mapperxml.ftl", saveUrl, entityName + "Mapper.xml");
        freeMarkerUtil.generate(root, "mapper.ftl", saveUrl, entityName + "Mapper.java");
        freeMarkerUtil.generate(root, "service.ftl", saveUrl, "I" + entityName + "Service.java");
        freeMarkerUtil.generate(root, "serviceimpl.ftl", saveUrl, entityName + "ServiceImpl.java");
        freeMarkerUtil.generate(root, "dto.ftl", saveUrl, entityName + "Dto.java");

        return true;
    }

    @Override
    public boolean createlist(String sobjectname) {
        try {
            if (sobjectname.indexOf(",") == -1) {
                return createone(sobjectname);
            } else {
                String[] arrObjectname = sobjectname.split(",");
                for (String objectname : arrObjectname) {
                    boolean b = this.createone(objectname);
                    if (!b) {
                        throw new SystemException("代码生成异常");
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean createone(String sobjectname) throws Exception {
        SysObject sysObject = sysObjectService.getSysObjectByObjectName(sobjectname);
        if (ObjectUtil.isEmpty(sysObject)) {
            throw new SystemException("对象名不能为空");
        }

        String table = sysObject.getSDbTable();
        String moduleid = sysObject.getSysModuleId();
        moduleid = moduleid.toLowerCase();

        boolean b = create(table, null, null, sobjectname, moduleid);

        return b;
    }
}
