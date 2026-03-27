package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.AnnouncementSend;
import com.hiking.treasure.domain.dto.create.AnnouncementSendCreateDTO;
import com.hiking.treasure.domain.dto.update.AnnouncementSendUpdateDTO;
import com.hiking.treasure.domain.dto.query.AnnouncementSendQueryDTO;
import com.hiking.treasure.domain.vo.AnnouncementSendVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface AnnouncementSendConvert {
    AnnouncementSend toEntity(AnnouncementSendCreateDTO dto);

    AnnouncementSend toEntity(AnnouncementSendUpdateDTO dto);

    AnnouncementSend toEntity(AnnouncementSendQueryDTO dto);

    AnnouncementSendVO toVO(AnnouncementSend entity);

    List<AnnouncementSendVO> toVOs(List<AnnouncementSend> list);
}
