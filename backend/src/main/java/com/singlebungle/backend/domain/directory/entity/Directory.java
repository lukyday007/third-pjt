package com.singlebungle.backend.domain.directory.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
@Table(name = "directory")
public class Directory extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "directory_id", unique = true, nullable = false)
    private Long directoryId;

    // 유저 번호
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 디렉토리명
    @Column(name = "directory_name")
    private String name;

    // 디렉토리 순서
    @Builder.Default
    @Column(name = "directory_order")
    private int order = 0;

    // 상태  (default : 0, nomal : 1, bin : 2)
    @Column(name = "status")
    private int status;


    // 디렉토리 새로 생성할 때
    public static Directory convertToEntity(String name) {
        int status = 1;
        Directory directory = new Directory();
        directory.setName(name);
        directory.setStatus(status);

        return directory;
    }
    // 디렉토리 순서 번경


}
