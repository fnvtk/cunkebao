package ${basePackageUrl}.${moduleId}.service.impl;

import ${basePackageUrl}.${moduleId}.entity.${tableName};
import ${basePackageUrl}.${moduleId}.mapper.${tableName}Mapper;
import ${basePackageUrl}.${moduleId}.service.I${tableName}Service;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ${tableName}ServiceImpl extends BusinessServiceImpl<${tableName}Mapper, ${tableName}> implements I${tableName}Service {

    private static final Logger logger = LoggerFactory.getLogger(${tableName}ServiceImpl.class);

}
