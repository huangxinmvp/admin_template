package com.hiking.treasure.service;

import com.hiking.treasure.domain.vo.system.DepartTreeVO;
import com.hiking.treasure.domain.vo.system.OptionVO;
import com.hiking.treasure.entity.Depart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 部门 服务类
 * </p>
 *
 * @author hx
 * @since 2025-09-04
 */
public interface DepartService extends IService<Depart> {
    List<DepartTreeVO> listTree();
    List<OptionVO> listOptions();
    boolean deleteDepartById(String departId);
    boolean deleteDepartsByIds(List<String> departIds);
}
