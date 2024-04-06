package cn.myerm.business.service.impl;

import cn.myerm.business.entity.Material;
import cn.myerm.business.entity.MiniProgrameLib;
import cn.myerm.business.mapper.MiniProgrameLibMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IMiniProgrameLibService;
import cn.myerm.common.exception.SystemException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MiniProgrameLibServiceImpl extends BusinessServiceImpl<MiniProgrameLibMapper, MiniProgrameLib> implements IMiniProgrameLibService {

    private static final Logger logger = LoggerFactory.getLogger(MiniProgrameLibServiceImpl.class);

    @Resource
    private MaterialServiceImpl materialService;

    /**
     * 小程序入库
     *
     * @param MaterialId
     * @param sName
     */
    public void add(Integer MaterialId, String sName) {
        Material material = materialService.getById(MaterialId);

        QueryWrapper<MiniProgrameLib> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MaterialId", MaterialId);
        if (count(queryWrapper) > 0) {
            throw new SystemException("该小程序已入库，不能重复入库。");
        }

        MiniProgrameLib miniProgrameLib = new MiniProgrameLib();
        miniProgrameLib.setSName(sName);
        miniProgrameLib.setNewUserId(getCurrUser().getLID());
        miniProgrameLib.setDNewTime(LocalDateTime.now());
        miniProgrameLib.setMaterialId(MaterialId);

        JSONObject jsonObject = JSONObject.parseObject(material.getSSourceContent());
        miniProgrameLib.setSThumb(jsonObject.getString("previewImage"));

        //提取miniprogramId
        String regex = "<username>(.*?)</username>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(material.getSSourceContent());
        if (matcher.find()) {
            jsonObject.put("miniprogramId", matcher.group(1));
        } else {
            regex = "<sourceusername>(.*?)</sourceusername>";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(material.getSSourceContent());
            if (matcher.find()) {
                jsonObject.put("miniprogramId", matcher.group(1));
            } else {
                throw new SystemException("小程序异常，请联系管理员");
            }
        }

        jsonObject.put("title", sName);
        miniProgrameLib.setSContent(jsonObject.toJSONString());

        save(miniProgrameLib);
    }

    public String appendWhereSql(ListParam listParam) {
        if (getCurrUser().getSysRoleId() > 1) {
            return super.appendWhereSql(listParam) + " AND NewUserId='" + getCurrUser().getLID() + "'";
        }

        return super.appendWhereSql(listParam);
    }
}
