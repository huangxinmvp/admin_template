package com.hiking.treasure.service;

import com.hiking.treasure.domain.vo.system.CurrentUserProfileVO;
import com.hiking.treasure.domain.vo.system.DashboardStatsVO;
import com.hiking.treasure.domain.vo.system.MenuTreeVO;

import java.util.List;

public interface SystemPortalService {
    CurrentUserProfileVO getCurrentUserProfile();

    List<MenuTreeVO> getCurrentUserMenus();

    DashboardStatsVO getDashboardStats();
}
