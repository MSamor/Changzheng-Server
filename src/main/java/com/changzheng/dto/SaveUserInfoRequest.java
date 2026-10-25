package com.changzheng.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data public class SaveUserInfoRequest {
 @NotNull private Long userId; private String avatarUrl; private String city; private String country; private Integer gender; private String nickname; private String province;
}
