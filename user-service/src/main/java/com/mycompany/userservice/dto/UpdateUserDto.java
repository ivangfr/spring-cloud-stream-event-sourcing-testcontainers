package com.mycompany.userservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UpdateUserDto {

    @ApiModelProperty(example = "ivan.franchin.jr@test2.com")
    @Email
    private String email;

    @ApiModelProperty(position = 1, example = "Ivan Franchin Jr")
    private String fullName;

    @ApiModelProperty(position = 2, example = "false")
    private Boolean active;

}
