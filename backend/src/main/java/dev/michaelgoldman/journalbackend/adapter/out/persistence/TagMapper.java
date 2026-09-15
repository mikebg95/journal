package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import dev.michaelgoldman.journalbackend.domain.model.Tag;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface TagMapper {

    List<Tag> toDomain(List<TagEntity> tagEntities);
}
