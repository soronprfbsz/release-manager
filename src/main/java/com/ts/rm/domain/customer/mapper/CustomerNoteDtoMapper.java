package com.ts.rm.domain.customer.mapper;

import com.ts.rm.domain.customer.dto.CustomerNoteDto;
import com.ts.rm.domain.customer.entity.CustomerNote;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * CustomerNote Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface CustomerNoteDtoMapper {

    @Mapping(target = "noteId", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createdByEmail", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "updatedByEmail", ignore = true)
    CustomerNote toEntity(CustomerNoteDto.CreateRequest request);

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "creator.accountName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "isDeletedCreator", expression = "java(note.getCreator() == null)")
    @Mapping(target = "updatedByEmail", source = "updatedByEmail")
    @Mapping(target = "updatedByName", source = "updater.accountName")
    @Mapping(target = "updatedByAvatarStyle", source = "updater.avatarStyle")
    @Mapping(target = "updatedByAvatarSeed", source = "updater.avatarSeed")
    @Mapping(target = "isDeletedUpdater", expression = "java(note.getUpdater() == null)")
    CustomerNoteDto.Response toResponse(CustomerNote note);

    List<CustomerNoteDto.Response> toResponseList(List<CustomerNote> notes);
}
