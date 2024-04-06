package cn.myerm.system.controller;


import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.service.impl.SysSolutionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-09-08
 */
@RestController
@RequestMapping("/v1/backend/system/syssolution")
public class SysSolutionController extends BusinessController {

    private final SysSolutionServiceImpl sysSolutionService;

    @Autowired
    public SysSolutionController(SysSolutionServiceImpl sysSolutionService) {
        this.businessService = sysSolutionService;
        this.sysSolutionService = sysSolutionService;
    }

    /**
     * 对象详情
     *
     * @return MessageDTO
     */
    @PostMapping("/detail")
    public MessageDTO view(@RequestParam(name = "objectid", required = true) String ObjectId) {
        return success(sysSolutionService.getDetailData(Integer.valueOf(ObjectId)));
    }

    @PostMapping("/navitem/all")
    public MessageDTO navitem() {
        return success(sysSolutionService.getAllNavItem());
    }

    @PostMapping("/navitem/newsave")
    public MessageDTO navItemNewSave(@RequestParam(name = "ObjectId", required = true) String ObjectId,
                                     @RequestParam(name = "sName", required = true) String sName,
                                     @RequestParam(name = "sIcon", required = true) String sIcon,
                                     @RequestParam(name = "arrChildren", required = true) String sNaveItemSelectedJson) {
        sysSolutionService.newNaveItemSave(ObjectId, sName, sIcon, sNaveItemSelectedJson);
        return success();
    }

    @PostMapping("/navitem/editsave")
    public MessageDTO navItemNewSave(@RequestParam(name = "id", required = true) String id,
                                     @RequestParam(name = "ObjectId", required = true) String ObjectId,
                                     @RequestParam(name = "sName", required = true) String sName,
                                     @RequestParam(name = "sIcon", required = true) String sIcon,
                                     @RequestParam(name = "arrChildren", required = true) String sNaveItemSelectedJson) {
        sysSolutionService.editNaveItemSave(ObjectId, id, sName, sIcon, sNaveItemSelectedJson);
        return success();
    }

    @PostMapping("/navitem/del")
    public MessageDTO del(@RequestParam(name = "objectid", required = true) String ObjectId,
                          @RequestParam(name = "selectedids", required = true) String sSelectIds) {
        sysSolutionService.del(ObjectId, sSelectIds);
        return success();
    }

    @PostMapping("/navitem/sortsave")
    public MessageDTO sortSave(@RequestParam(name = "objectid", required = true) String ObjectId,
                               @RequestParam(name = "id", required = true) String id,
                               @RequestParam(name = "arrChildren", required = true) String sChildren) {
        sysSolutionService.sortSave(ObjectId, id, sChildren);
        return success();
    }

    @PostMapping("/navitem/menusortsave")
    public MessageDTO menuSortSave(@RequestParam(name = "objectid", required = true) String ObjectId,
                                @RequestParam(name = "sortdata", required = true) String sSortData) {
        sysSolutionService.menuSortSave(ObjectId, sSortData);
        return success();
    }

}
