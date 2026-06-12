package com.classmanager.cms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subtopics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subtopic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "no_of_questions")
    private Integer noOfQuestions;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
