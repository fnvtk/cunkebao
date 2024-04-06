package cn.myerm.business.controller;

import cn.myerm.business.service.impl.DownloadVideoScheduleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/downloadvideoschedule")
public class DownloadVideoScheduleController extends BusinessController {

    private final DownloadVideoScheduleServiceImpl downloadvideoscheduleService;

    @Autowired
    public DownloadVideoScheduleController(DownloadVideoScheduleServiceImpl downloadvideoscheduleService) {
        this.businessService = downloadvideoscheduleService;
        this.downloadvideoscheduleService = downloadvideoscheduleService;
    }
}
