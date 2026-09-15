package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import dev.michaelgoldman.journalbackend.application.port.out.TagStore;
import dev.michaelgoldman.journalbackend.domain.model.Tag;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
class JpaTagStore implements TagStore {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    JpaTagStore(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Override
    public void ensureExist(Set<Tag> tags) {}

    @Override
    public List<Tag> findAll() {
        List<TagEntity> tagEntities = tagRepository.findAllByOrderByValueAsc();
        return tagMapper.toDomain(tagEntities);
    }
}
