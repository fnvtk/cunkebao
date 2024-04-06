package cn.myerm.alertcenter.service.impl;

import cn.myerm.alertcenter.entity.ErrorTask;
import cn.myerm.alertcenter.mapper.ErrorTaskMapper;
import cn.myerm.alertcenter.service.IErrorTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ErrorTaskServiceImpl extends ServiceImpl<ErrorTaskMapper, ErrorTask> implements IErrorTaskService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorTaskServiceImpl.class);
}
