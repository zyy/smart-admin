package net.lab1024.smartadmin.module.system.role.service;

import com.google.common.collect.Lists;
import net.lab1024.smartadmin.common.code.UserErrorCode;
import net.lab1024.smartadmin.common.domain.ResponseDTO;
import net.lab1024.smartadmin.common.util.SmartBeanUtil;
import net.lab1024.smartadmin.module.system.menu.constant.MenuConst;
import net.lab1024.smartadmin.module.system.menu.dao.MenuDao;
import net.lab1024.smartadmin.module.system.menu.domain.entity.MenuEntity;
import net.lab1024.smartadmin.module.system.menu.domain.vo.MenuSimpleTreeVO;
import net.lab1024.smartadmin.module.system.menu.domain.vo.MenuVO;
import net.lab1024.smartadmin.module.system.role.dao.RoleDao;
import net.lab1024.smartadmin.module.system.role.dao.RoleMenuDao;
import net.lab1024.smartadmin.module.system.role.domain.entity.RoleEntity;
import net.lab1024.smartadmin.module.system.role.domain.entity.RoleMenuEntity;
import net.lab1024.smartadmin.module.system.role.domain.form.RoleMenuUpdateForm;
import net.lab1024.smartadmin.module.system.role.domain.vo.RoleMenuTreeVO;
import net.lab1024.smartadmin.module.system.role.manager.RoleMenuManager;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色-菜单
 *
 * @author 李善逸
 */
@Service
public class RoleMenuService {

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private RoleMenuDao roleMenuDao;
    @Autowired
    private RoleMenuManager roleMenuManager;
    @Autowired
    private MenuDao menuDao;

    /**
     * 更新角色权限
     *
     * @param roleMenuUpdateForm
     * @return
     */
    public ResponseDTO<String> updateRoleMenu(RoleMenuUpdateForm roleMenuUpdateForm) {
        //查询角色是否存在
        Long roleId = roleMenuUpdateForm.getRoleId();
        RoleEntity roleEntity = roleDao.selectById(roleId);
        if (null == roleEntity) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        List<RoleMenuEntity> roleMenuEntityList = Lists.newArrayList();
        RoleMenuEntity roleMenuEntity;
        for (Long menuId : roleMenuUpdateForm.getMenuIdList()) {
            roleMenuEntity = new RoleMenuEntity();
            roleMenuEntity.setRoleId(roleId);
            roleMenuEntity.setMenuId(menuId);
            roleMenuEntityList.add(roleMenuEntity);
        }
        roleMenuManager.updateRoleMenu(roleMenuUpdateForm.getRoleId(), roleMenuEntityList);
        return ResponseDTO.ok();
    }

    /**
     * 根据角色id集合，查询其所有的菜单权限
     * @param roleIdList
     * @return
     */
    public List<MenuVO> getMenuList(List<Long> roleIdList) {
        if (CollectionUtils.isEmpty(roleIdList)) {
            return new ArrayList<>();
        }

        List<MenuEntity> menuEntityList = roleMenuDao.selectMenuListByRoleIdList(roleIdList);
        return SmartBeanUtil.copyList(menuEntityList, MenuVO.class);
    }


    /**
     * 获取角色关联菜单权限
     *
     * @param roleId
     * @return
     */
    public ResponseDTO<RoleMenuTreeVO> getRoleSelectedMenu(Long roleId) {
        RoleMenuTreeVO res = new RoleMenuTreeVO();
        res.setRoleId(roleId);
        //查询角色ID选择的菜单权限
        List<Long> selectedMenuId = roleMenuDao.queryMenuIdByRoleId(roleId);
        res.setSelectedMenuId(selectedMenuId);
        //查询菜单权限
        List<MenuVO> menuVOList = menuDao.queryMenuList(Boolean.FALSE, Boolean.FALSE, null);
        Map<Long, List<MenuVO>> parentMap = menuVOList.stream().collect(Collectors.groupingBy(MenuVO::getParentId, Collectors.toList()));
        List<MenuSimpleTreeVO> menuTreeList = this.buildMenuTree(parentMap, MenuConst.DEFAULT_MENU_PARENT_ID);
        res.setMenuTreeList(menuTreeList);
        return ResponseDTO.ok(res);
    }

    /**
     * 构建菜单树
     *
     * @return
     */
    private List<MenuSimpleTreeVO> buildMenuTree(Map<Long, List<MenuVO>> parentMap, Long parentId) {
        // 获取本级菜单树List
        List<MenuSimpleTreeVO> res = parentMap.getOrDefault(parentId, Lists.newArrayList()).stream()
                .map(e -> SmartBeanUtil.copy(e, MenuSimpleTreeVO.class)).collect(Collectors.toList());
        // 循环遍历下级菜单
        res.forEach(e -> {
            e.setChildren(this.buildMenuTree(parentMap, e.getMenuId()));
        });
        return res;
    }
}