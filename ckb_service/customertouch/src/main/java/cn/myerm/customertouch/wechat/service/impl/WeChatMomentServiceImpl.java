package cn.myerm.customertouch.wechat.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.myerm.customertouch.wechat.entity.WeChatMoment;
import cn.myerm.customertouch.wechat.mapper.WeChatMomentMapper;
import cn.myerm.customertouch.wechat.param.MomentListParam;
import cn.myerm.customertouch.wechat.param.MomentPublishParam;
import cn.myerm.customertouch.wechat.service.IWeChatMomentService;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WeChatMomentServiceImpl extends ServiceImpl<WeChatMomentMapper, WeChatMoment> implements IWeChatMomentService {
    public List<Map<String, Object>> list(MomentListParam momentListParam) {
        QueryWrapper<WeChatMoment> queryWrapper = new QueryWrapper<>();

        if (StrUtil.isNotEmpty(momentListParam.getStarttime())) {
            queryWrapper.ge("collectTime", momentListParam.getStarttime());
        }

        queryWrapper.ge("status", 2);
        queryWrapper.last(" LIMIT " + momentListParam.getPagesize() + " OFFSET " + (momentListParam.getPage() - 1) * momentListParam.getPagesize());

        List<Map<String, Object>> listMaps = listMaps(queryWrapper);
        for (Map<String, Object> mapMoment : listMaps) {
            mapMoment.put("commentList", JSONArray.parseArray(mapMoment.get("commentList").toString()));
            mapMoment.put("likeList", JSONArray.parseArray(mapMoment.get("likeList").toString()));
            mapMoment.put("urls", JSONArray.parseArray(mapMoment.get("urls").toString()));
        }

        return listMaps;
    }
}
