package com.singlebungle.backend.domain.directory.entity;

import com.singlebungle.backend.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Table(name = "directory")
public class Directory extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "directory_id", unique = true, nullable = false)
    private Long directoryId;

//    // 유저 번호
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    // 디렉토리 명
    @Column(name = "directory_name")
    private String name;

    // 디렉토리 순서
    @Column(name = "directory_order")
    private int order = 0;

    // 상태 enum (default, nomal, bin)
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
