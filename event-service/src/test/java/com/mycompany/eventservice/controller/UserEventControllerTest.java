package com.mycompany.eventservice.controller;

import com.mycompany.eventservice.config.ModelMapperConfig;
import com.mycompany.eventservice.model.UserEvent;
import com.mycompany.eventservice.model.UserEventKey;
import com.mycompany.eventservice.service.UserEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.mycompany.eventservice.util.MyLocalDateHandler.fromDateToString;
import static com.mycompany.eventservice.util.MyLocalDateHandler.fromStringToDate;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserEventController.class)
@Import(ModelMapperConfig.class)
public class UserEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserEventService userEventService;

    @Test
    public void given_noUserEvents_when_getUserEventByUserId_then_returnEmptyJsonArray() throws Exception {
        Long userId = 1L;
        given(userEventService.getAllUserEventsByUserId(userId)).willReturn(new ArrayList<>());

        ResultActions resultActions = mockMvc.perform(get("/api/events/users/"+ userId)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void given_oneUserEvent_when_getUserEventByUserId_then_returnArrayWithUserEventJson() throws Exception {
        Long userId = 1L;
        Date datetime = fromStringToDate("2018-12-03T10:15:30+0100");
        String data = "data123";
        String type = "type123";
        UserEvent userEvent = new UserEvent(new UserEventKey(userId, datetime), type, data);

        given(userEventService.getAllUserEventsByUserId(userId)).willReturn(Arrays.asList(userEvent));

        ResultActions resultActions = mockMvc.perform(get("/api/events/users/"+ userId)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].datetime", is(fromDateToString(datetime))))
                .andExpect(jsonPath("$[0].data", is(data)))
                .andExpect(jsonPath("$[0].type", is(type)));
    }

}