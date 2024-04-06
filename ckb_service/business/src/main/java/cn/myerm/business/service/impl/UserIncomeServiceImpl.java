package cn.myerm.business.service.impl;

import cn.myerm.business.entity.UserIncome;
import cn.myerm.business.mapper.UserIncomeMapper;
import cn.myerm.business.service.IUserIncomeService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserIncomeServiceImpl extends BusinessServiceImpl<UserIncomeMapper, UserIncome> implements IUserIncomeService {

    private static final Logger logger = LoggerFactory.getLogger(UserIncomeServiceImpl.class);

}
