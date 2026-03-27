package com.hiking.treasure.domain.convert;

import org.mapstruct.Mapper;
import com.hiking.treasure.entity.File;
import com.hiking.treasure.domain.dto.create.FileCreateDTO;
import com.hiking.treasure.domain.dto.update.FileUpdateDTO;
import com.hiking.treasure.domain.dto.query.FileQueryDTO;
import com.hiking.treasure.domain.vo.FileVO;

import java.util.List;

@Mapper(componentModel = "spring" )
public interface FileConvert {
    File toEntity(FileCreateDTO dto);

    File toEntity(FileUpdateDTO dto);

    File toEntity(FileQueryDTO dto);

    FileVO toVO(File entity);

    List<FileVO> toVOs(List<File> list);
}
