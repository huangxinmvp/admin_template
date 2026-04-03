package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.vo.ApprovalActionLogVO;
import com.hiking.treasure.modules.phase1.entity.ApprovalActionLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApprovalActionLogConvert {

    ApprovalActionLogVO toVO(ApprovalActionLog entity);

    List<ApprovalActionLogVO> toVOs(List<ApprovalActionLog> entities);
}
