package dev.michaelgoldman.journalbackend.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface TagRepository extends JpaRepository<TagEntity, Long> {
    List<TagEntity> findAllByOrderByValueAsc();
}
