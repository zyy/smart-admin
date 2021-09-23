package net.lab1024.smartadmin.service.module.support.operatelog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.smartadmin.service.common.domain.PageResultDTO;
import net.lab1024.smartadmin.service.common.domain.ResponseDTO;
import net.lab1024.smartadmin.service.module.support.operatelog.domain.dto.OperateLogDTO;
import net.lab1024.smartadmin.service.module.support.operatelog.domain.OperateLogEntity;
import net.lab1024.smartadmin.service.module.support.operatelog.domain.dto.OperateLogQueryDTO;
import net.lab1024.smartadmin.service.util.SmartBeanUtil;
import net.lab1024.smartadmin.service.util.SmartPageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [  ]
 *
 * @author 罗伊
 */
@Service
public class OperateLogService {

    @Autowired
    private OperateLogDao operateLogDao;

    /**
     * @author 罗伊
     * @description 分页查询
     * @date 2019-05-15 11:32:14
     */
    public ResponseDTO<PageResultDTO<OperateLogDTO>> queryByPage(OperateLogQueryDTO queryDTO) {
        Page page = SmartPageUtil.convert2PageQuery(queryDTO);
        List<OperateLogEntity> entities = operateLogDao.queryByPage(page, queryDTO);
        List<OperateLogDTO> dtoList = SmartBeanUtil.copyList(entities, OperateLogDTO.class);
        page.setRecords(dtoList);
        PageResultDTO<OperateLogDTO> pageResultDTO = SmartPageUtil.convert2PageResult(page);
        return ResponseDTO.succData(pageResultDTO);
    }

    /**
     * @author 罗伊
     * @description 添加
     * @date 2019-05-15 11:32:14
     */
    public ResponseDTO<String> add(OperateLogDTO addDTO) {
        OperateLogEntity entity = SmartBeanUtil.copy(addDTO, OperateLogEntity.class);
        operateLogDao.insert(entity);
        return ResponseDTO.succ();
    }

    /**
     * @author 罗伊
     * @description 编辑
     * @date 2019-05-15 11:32:14
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(OperateLogDTO updateDTO) {
        OperateLogEntity entity = SmartBeanUtil.copy(updateDTO, OperateLogEntity.class);
        operateLogDao.updateById(entity);
        return ResponseDTO.succ();
    }

    /**
     * @author 罗伊
     * @description 删除
     * @date 2019-05-15 11:32:14
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delete(Long id) {
        operateLogDao.deleteById(id);
        return ResponseDTO.succ();
    }

    /**
     * @author 罗伊
     * @description 根据ID查询
     * @date 2019-05-15 11:32:14
     */
    public ResponseDTO<OperateLogDTO> detail(Long id) {
        OperateLogEntity entity = operateLogDao.selectById(id);
        OperateLogDTO dto = SmartBeanUtil.copy(entity, OperateLogDTO.class);
        return ResponseDTO.succData(dto);
    }
}