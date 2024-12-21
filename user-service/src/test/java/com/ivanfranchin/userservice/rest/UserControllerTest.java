package com.ivanfranchin.userservice.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ivanfranchin.userservice.exception.UserEmailDuplicatedException;
import com.ivanfranchin.userservice.exception.UserNotFoundException;
import com.ivanfranchin.userservice.kafka.UserStream;
import com.ivanfranchin.userservice.model.User;
import com.ivanfranchin.userservice.rest.dto.CreateUserRequest;
import com.ivanfranchin.userservice.rest.dto.UpdateUserRequest;
import com.ivanfranchin.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserStream userStream;

    @Test
    void testGetUsersWhenThereIsNone() throws Exception {
        given(userService.getUsers()).willReturn(Collections.emptyList());

        ResultActions resultActions = mockMvc.perform(get(API_USERS_URL))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$, hasSize(0)));
    }

    @Test
    void testGetUsersWhenThereIsOne() throws Exception {
        User user = getDefaultUser();
        given(userService.getUsers()).willReturn(Collections.singletonList(user));

        ResultActions resultActions = mockMvc.perform(get(API_USERS_URL))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$, hasSize(1)))
                .andExpect(jsonPath(JSON_$_0_ID, is(user.getId().intValue())))
                .andExpect(jsonPath(JSON_$_0_EMAIL, is(user.getEmail())))
                .andExpect(jsonPath(JSON_$_0_FULL_NAME, is(user.getFullName())))
                .andExpect(jsonPath(JSON_$_0_ACTIVE, is(user.getActive())));
    }

    @Test
    void testGetUserByIdWhenNonExistent() throws Exception {
        given(userService.validateAndGetUserById(anyLong())).willThrow(UserNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(get(API_USERS_ID_URL, 1))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void testGetUserByIdWhenExistent() throws Exception {
        User user = getDefaultUser();
        given(userService.validateAndGetUserById(anyLong())).willReturn(user);

        ResultActions resultActions = mockMvc.perform(get(API_USERS_ID_URL, 1))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
                .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
                .andExpect(jsonPath(JSON_$_FULL_NAME, is(user.getFullName())))
                .andExpect(jsonPath(JSON_$_ACTIVE, is(user.getActive())));
    }

    @Test
    void testCreateUserInformingValidInput() throws Exception {
        User user = getDefaultUser();
        given(userService.saveUser(any(User.class))).willReturn(user);

        CreateUserRequest createUserRequest = getDefaultCreateUserRequest();
        ResultActions resultActions = mockMvc.perform(post(API_USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
                .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
                .andExpect(jsonPath(JSON_$_FULL_NAME, is(user.getFullName())))
                .andExpect(jsonPath(JSON_$_ACTIVE, is(user.getActive())));
    }

    @Test
    void testCreateUserInformingInvalidInput() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(API_USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(null, null, null))))
                .andDo(print());

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserWhenThereIsDuplicatedEmail() throws Exception {
        willThrow(UserEmailDuplicatedException.class).given(userService).validateUserExistsByEmail(anyString());

        ResultActions resultActions = mockMvc.perform(post(API_USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getDefaultCreateUserRequest())))
                .andDo(print());

        resultActions.andExpect(status().isConflict());
    }

    @Test
    void testUpdateUserWhenExistent() throws Exception {
        User user = getDefaultUser();
        given(userService.validateAndGetUserById(anyLong())).willReturn(user);
        given(userService.saveUser(any(User.class))).willReturn(user);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest("email2@test", "fullName2", false);

        ResultActions resultActions = mockMvc.perform(put(API_USERS_ID_URL, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
                .andExpect(jsonPath(JSON_$_EMAIL, is(updateUserRequest.email())))
                .andExpect(jsonPath(JSON_$_FULL_NAME, is(updateUserRequest.fullName())))
                .andExpect(jsonPath(JSON_$_ACTIVE, is(updateUserRequest.active())));
    }

    @Test
    void testUpdateUserWhenNonExistent() throws Exception {
        given(userService.validateAndGetUserById(anyLong())).willThrow(UserNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(put(API_USERS_ID_URL, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest(null, null, null))))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUserWhenExistent() throws Exception {
        User user = getDefaultUser();
        given(userService.validateAndGetUserById(anyLong())).willReturn(user);

        ResultActions resultActions = mockMvc.perform(delete(API_USERS_ID_URL, user.getId()))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(user.getId().intValue())))
                .andExpect(jsonPath(JSON_$_EMAIL, is(user.getEmail())))
                .andExpect(jsonPath(JSON_$_FULL_NAME, is(user.getFullName())))
                .andExpect(jsonPath(JSON_$_ACTIVE, is(user.getActive())));
    }

    @Test
    void testDeleteUserWhenNonExistent() throws Exception {
        User user = getDefaultUser();
        given(userService.validateAndGetUserById(anyLong())).willThrow(UserNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(delete(API_USERS_ID_URL, user.getId()))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    private User getDefaultUser() {
        User user = new User("email@test", "fullName", true);
        user.setId(1L);
        return user;
    }

    private CreateUserRequest getDefaultCreateUserRequest() {
        return new CreateUserRequest("email@test", "fullName", true);
    }

    private static final String API_USERS_URL = "/api/users";
    private static final String API_USERS_ID_URL = "/api/users/{id}";

    private static final String JSON_$ = "$";

    private static final String JSON_$_ID = "$.id";
    private static final String JSON_$_EMAIL = "$.email";
    private static final String JSON_$_FULL_NAME = "$.fullName";
    private static final String JSON_$_ACTIVE = "$.active";

    private static final String JSON_$_0_ID = "$[0].id";
    private static final String JSON_$_0_EMAIL = "$[0].email";
    private static final String JSON_$_0_FULL_NAME = "$[0].fullName";
    private static final String JSON_$_0_ACTIVE = "$[0].active";
}