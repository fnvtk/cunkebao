package cn.myerm.business.service.impl;

import cn.myerm.business.entity.JdPromotionSite;
import cn.myerm.business.entity.JdSocialMedia;
import cn.myerm.business.mapper.JdSocialMediaMapper;
import cn.myerm.business.service.IJdSocialMediaService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JdSocialMediaServiceImpl extends BusinessServiceImpl<JdSocialMediaMapper, JdSocialMedia> implements IJdSocialMediaService {

    private static final Logger logger = LoggerFactory.getLogger(JdSocialMediaServiceImpl.class);

    private final JdPromotionSiteServiceImpl jdPromotionSiteService;

    public JdSocialMediaServiceImpl(JdPromotionSiteServiceImpl jdPromotionSiteService) {
        this.jdPromotionSiteService = jdPromotionSiteService;
    }

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();
        JSONObject btn = new JSONObject();

        btn.put("ID", "import");
        btn.put("sName", "导入");
        btn.put("handler", "handleJdSocialMediaImport");
        btn.put("icon", "el-icon-upload2");
        arrBtn.add(btn);

        arrBtn.addAll(super.getListTableBtn(mapParam));

        return arrBtn;
    }

    public void importSave(String data) {
        JSONArray jsonArray = JSONArray.parseArray(data);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            JdSocialMedia jdSocialMedia = new JdSocialMedia();
            jdSocialMedia.setLId(jsonObject.getLongValue("siteId"));
            jdSocialMedia.setSName(jsonObject.getString("siteName"));
            saveOrUpdate(jdSocialMedia);

            JdPromotionSite jdPromotionSite = new JdPromotionSite();
            jdPromotionSite.setLId(jsonObject.getLongValue("id"));
            jdPromotionSite.setSName(jsonObject.getString("promotionName"));
            jdPromotionSite.setJdSocialMediaId(jsonObject.getLongValue("siteId"));
            jdPromotionSiteService.saveOrUpdate(jdPromotionSite);
        }
    }
}
