package ${basePackageUrl}.${moduleId}.controller;

import ${basePackageUrl}.${moduleId}.service.impl.${entityName}ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/${objecNameLower}")
public class ${entityName}Controller extends BusinessController {

    private final ${entityName}ServiceImpl ${entityNameLower}Service;

    @Autowired
    public ${entityName}Controller(${entityName}ServiceImpl ${entityNameLower}Service) {
        this.businessService = ${entityNameLower}Service;
        this.${entityNameLower}Service = ${entityNameLower}Service;
    }
}
