package cn.myerm.business.service.impl;

import cn.myerm.business.entity.Cache;
import cn.myerm.business.mapper.CacheMapper;
import cn.myerm.business.service.ICacheService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CacheServiceImpl extends BusinessServiceImpl<CacheMapper, Cache> implements ICacheService {
    public String getOrSet(String sKey, String sDefaultValue) {
        Cache cache = getById(sKey);
        if (cache == null) {
            cache = new Cache();
            cache.setSKey(sKey);
            cache.setSValue(sDefaultValue);
            save(cache);

            return sDefaultValue;
        } else {
            return cache.getSValue();
        }
    }

    public void set(String sKey, String sNewValue) {
        Cache cache = new Cache();
        cache.setSKey(sKey);
        cache.setSValue(sNewValue);
        saveOrUpdate(cache);
    }

    public String get(String sKey) {
        Cache cache = getById(sKey);
        if (cache == null) {
            return null;
        } else {
            return cache.getSValue();
        }
    }

    /**
     * 通过前缀获取数据
     * @param sPrefix
     * @param lLimit
     * @return
     */
    public List<Cache> getByPrefix(String sPrefix, int lStart, int lLimit) {
        QueryWrapper<Cache> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.likeRight("sKey", sPrefix);
        objectQueryWrapper.last("LIMIT " + lStart + "," + lLimit);

        return list(objectQueryWrapper);
    }

    /**
     * 通过前缀删除数据
     * @param sPrefix
     */
    public void removeByPrefix(String sPrefix) {
        QueryWrapper<Cache> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.likeRight("sKey", sPrefix);
        remove(objectQueryWrapper);
    }
}
