package cn.myerm.business.controller;

import cn.myerm.business.migrate.OldToNew;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/migrate")
public class MigrateController {

    private final OldToNew oldToNew;

    @Autowired
    public MigrateController(OldToNew oldToNew) {
        this.oldToNew = oldToNew;
    }

    //@PostMapping("/oldtonew")
    public void oldtonew() {
        oldToNew.exec();
    }
}
