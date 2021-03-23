package com.mycompany.eventservice.rest;

import com.mycompany.eventservice.mapper.UserMapperImpl;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.mycompany.eventservice.util.MyLocalDateHandler.fromDateToString;
import static com.mycompany.eventservice.util.MyLocalDateHandler.fromStringToDate;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserEventController.class)
@Import(UserMapperImpl.class)
class UserEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserEventService userEventService;

    @Test
    void givenNoUserEventsWhenGetUserEventByUserIdThenReturnEmptyJsonArray() throws Exception {
        Long userId = 1L;
        given(userEventService.getAllUserEvents(userId)).willReturn(new ArrayList<>());

        ResultActions resultActions = mockMvc.perform(get("/api/events/users/" + userId))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenOneUserEventWhenGetUserEventByUserIdThenReturnArrayWithUserEventJson() throws Exception {
        Long userId = 1L;
        Date datetime = fromStringToDate("2018-12-03T10:15:30.000+0100");
        String data = "data123";
        String type = "type123";
        UserEvent userEvent = new UserEvent(new UserEventKey(userId, datetime), type, data);

        given(userEventService.getAllUserEvents(userId)).willReturn(Collections.singletonList(userEvent));

        ResultActions resultActions = mockMvc.perform(get("/api/events/users/" + userId))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].datetime", is(fromDateToString(datetime))))
                .andExpect(jsonPath("$[0].data", is(data)))
                .andExpect(jsonPath("$[0].type", is(type)));
    }

}