package com.ivanfranchin.eventservice.userevent;

import com.ivanfranchin.eventservice.userevent.model.UserEvent;
import com.ivanfranchin.eventservice.userevent.model.UserEventKey;
import com.ivanfranchin.eventservice.util.MyLocalDateHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserEventController.class)
class UserEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserEventService userEventService;

    @Test
    void testGetUserEventsWhenThereIsNone() throws Exception {
        given(userEventService.getUserEvents(anyLong())).willReturn(Collections.emptyList());

        ResultActions resultActions = mockMvc.perform(get("/api/events?userId=1"))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetUserEventsWhenThereIsOne() throws Exception {
        UserEvent userEvent = new UserEvent(new UserEventKey(1L, new Date()), "type", "data");

        given(userEventService.getUserEvents(anyLong())).willReturn(Collections.singletonList(userEvent));

        ResultActions resultActions = mockMvc.perform(get("/api/events?userId=" + 1))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(userEvent.getKey().getUserId().intValue())))
                .andExpect(jsonPath("$[0].datetime", is(MyLocalDateHandler.fromDateToString(userEvent.getKey().getDatetime()))))
                .andExpect(jsonPath("$[0].data", is(userEvent.getData())))
                .andExpect(jsonPath("$[0].type", is(userEvent.getType())));
    }
}