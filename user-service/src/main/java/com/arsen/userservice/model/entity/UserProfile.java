package com.arsen.userservice.model.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "user_profiles")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseEntity {
    @Column(nullable = false, name = "first_name")
    private String firstName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(nullable = false, name = "last_name")
    private String lastName;
    @Column(nullable = false, name = "birthday")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date birthday;
    @Column(nullable = false, name = "cell_phone_number")
    private String cellPhoneNumber;
    @OneToOne(mappedBy = "userProfile")
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public UserProfile(String firstName, String lastName, Date birthday, String cellPhoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public UserProfile(String firstName, String middleName, String lastName, Date birthday, String cellPhoneNumber) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.cellPhoneNumber = cellPhoneNumber;
    }
}
