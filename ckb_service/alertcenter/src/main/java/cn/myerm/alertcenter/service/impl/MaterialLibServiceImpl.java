package cn.myerm.alertcenter.service.impl;

import cn.myerm.alertcenter.entity.MaterialLib;
import cn.myerm.alertcenter.mapper.MaterialLibMapper;
import cn.myerm.alertcenter.service.IMaterialLibService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MaterialLibServiceImpl extends ServiceImpl<MaterialLibMapper, MaterialLib> implements IMaterialLibService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialLibServiceImpl.class);
}
