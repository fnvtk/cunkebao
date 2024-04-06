package cn.myerm.business.service.impl;

import cn.myerm.business.entity.JdPromotionSite;
import cn.myerm.business.mapper.JdPromotionSiteMapper;
import cn.myerm.business.service.IJdPromotionSiteService;
import cn.myerm.common.dto.MessageDTO;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class JdPromotionSiteServiceImpl extends BusinessServiceImpl<JdPromotionSiteMapper, JdPromotionSite> implements IJdPromotionSiteService {

    private static final Logger logger = LoggerFactory.getLogger(JdPromotionSiteServiceImpl.class);

}
