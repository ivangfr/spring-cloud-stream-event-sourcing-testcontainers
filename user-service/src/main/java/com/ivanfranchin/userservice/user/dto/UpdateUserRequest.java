package com.ivanfranchin.userservice.user.dto;

import com.ivanfranchin.userservice.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
    @Schema(example = "ivan.franchin.2@test.com") @Email String email,
    @Schema(example = "Ivan Franchin 2") String fullName,
    @Schema(example = "false") Boolean active) {

  public void applyTo(User user) {
    if (email != null) {
      user.setEmail(email);
    }
    if (fullName != null) {
      user.setFullName(fullName);
    }
    if (active != null) {
      user.setActive(active);
    }
  }
}
