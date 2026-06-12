package com.classmanager.cms_backend.repository;

import com.classmanager.cms_backend.entity.Subtopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubtopicRepository extends JpaRepository<Subtopic, UUID> {

    @Query("""
           SELECT st FROM Subtopic st
           JOIN FETCH st.chapter ch
           JOIN FETCH ch.subject s
           JOIN FETCH ch.course c
           WHERE c.id IN :courseIds
             AND st.isDeleted = false AND ch.isDeleted = false AND s.isDeleted = false AND c.isDeleted = false
           """)
    List<Subtopic> findSubtopicsForCourses(@Param("courseIds") List<UUID> courseIds);
}
