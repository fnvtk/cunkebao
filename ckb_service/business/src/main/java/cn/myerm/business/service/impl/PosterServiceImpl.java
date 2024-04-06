package cn.myerm.business.service.impl;

import cn.myerm.business.entity.Poster;
import cn.myerm.business.mapper.PosterMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IPosterService;
import cn.myerm.system.entity.SysAttach;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PosterServiceImpl extends BusinessServiceImpl<PosterMapper, Poster> implements IPosterService {

    private static final Logger logger = LoggerFactory.getLogger(PosterServiceImpl.class);

    @Resource
    private SysAttachServiceImpl sysAttachService;

    public String appendWhereSql(ListParam listParam) {
        return super.appendWhereSql(listParam) + " AND (NewUserId='" + getCurrUser().getLID() + "' OR NewUserId='1')";
    }

    public List<Poster> getAll() {
        QueryWrapper<Poster> queryWrapper = new QueryWrapper<>();
        queryWrapper.last(" WHERE NewUserId='" + getCurrUser().getLID() + "' OR NewUserId='1'");
        List<Poster> posters = list(queryWrapper);
        for (Poster poster : posters) {
            SysAttach thumb = sysAttachService.getById(poster.getSThumb());
            poster.setSUrl(thumb.getSCdnUrl());
        }

        return posters;
    }

    public Poster getById(Integer id) {
        Poster poster = super.getById(id);
        SysAttach thumb = sysAttachService.getById(poster.getSThumb());
        poster.setSUrl(thumb.getSCdnUrl());

        return poster;
    }

    public Integer newPosterSave(String sName, Integer thumbId) {
        Poster poster = new Poster();
        poster.setDNewTime(LocalDateTime.now());
        poster.setNewUserId(getCurrUser().getLID());
        poster.setSName(sName);
        poster.setSThumb(thumbId);
        save(poster);

        return poster.getLId();
    }
}
