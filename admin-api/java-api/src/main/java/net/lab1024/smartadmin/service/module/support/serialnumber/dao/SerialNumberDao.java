package net.lab1024.smartadmin.service.module.support.serialnumber.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.smartadmin.service.module.support.serialnumber.domain.SerialNumberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * id生成 dao
 *
 * @author zhuo
 */
@Mapper
@Component
public interface SerialNumberDao extends BaseMapper<SerialNumberEntity> {

    /**
     * 排他锁查询
     *
     * @param id
     * @return
     */
    SerialNumberEntity selectForUpdate(@Param("id") Integer id);

    /**
     * 更新上一次的 数值和时间
     *
     * @param serialNumberId
     * @param lastTime
     */
    void updateLastNumberAndTime(@Param("Integer") Integer serialNumberId, @Param("lastNumber") Long lastNumber, @Param("lastTime") LocalDateTime lastTime);

}
