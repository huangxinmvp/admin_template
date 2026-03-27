package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.Announcement;
import com.hiking.treasure.domain.dto.create.AnnouncementCreateDTO;
import com.hiking.treasure.domain.dto.update.AnnouncementUpdateDTO;
import com.hiking.treasure.domain.dto.query.AnnouncementQueryDTO;
import com.hiking.treasure.domain.vo.AnnouncementVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface AnnouncementConvert {
    Announcement toEntity(AnnouncementCreateDTO dto);

    Announcement toEntity(AnnouncementUpdateDTO dto);

    Announcement toEntity(AnnouncementQueryDTO dto);

    AnnouncementVO toVO(Announcement entity);

    List<AnnouncementVO> toVOs(List<Announcement> list);
}
