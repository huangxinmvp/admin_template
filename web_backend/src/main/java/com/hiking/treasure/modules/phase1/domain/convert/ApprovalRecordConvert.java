package com.hiking.treasure.modules.phase1.domain.convert;

import com.hiking.treasure.modules.phase1.domain.dto.create.ApprovalRecordCreateDTO;
import com.hiking.treasure.modules.phase1.domain.dto.query.ApprovalRecordQueryDTO;
import com.hiking.treasure.modules.phase1.domain.dto.update.ApprovalRecordUpdateDTO;
import com.hiking.treasure.modules.phase1.domain.vo.ApprovalRecordVO;
import com.hiking.treasure.modules.phase1.entity.ApprovalRecord;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApprovalRecordConvert {

    ApprovalRecord toEntity(ApprovalRecordCreateDTO dto);

    ApprovalRecord toEntity(ApprovalRecordUpdateDTO dto);

    ApprovalRecord toEntity(ApprovalRecordQueryDTO dto);

    ApprovalRecordVO toVO(ApprovalRecord entity);

    List<ApprovalRecordVO> toVOs(List<ApprovalRecord> entities);
}
