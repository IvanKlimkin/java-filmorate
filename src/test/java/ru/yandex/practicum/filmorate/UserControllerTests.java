package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.adapter.LocalDateAdapter;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WebMvcTest(controllers = UserController.class)
class UserControllerTests {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Autowired
    MockMvc mockMvc;

    private final UserForTest noEmailUser = UserForTest.builder()
            .id(100)
            .email("")
            .login("Emailess")
            .name("Name 100")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();

    private final UserForTest badEmailFormatUser = UserForTest.builder()
            .id(100)
            .email("karAPUZru.ya")
            .login("BadEmail")
            .name("Name 101")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();

    private final UserForTest noLoginUser = UserForTest.builder()
            .id(102)
            .email("ya@ya.ru")
            .login("")
            .name("Name 102")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();

    private final UserForTest noLoginFillUser = UserForTest.builder()
            .id(103)
            .email("ya@ya.ru")
            .login("Bad Login")
            .name("Name 103")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();

    private final UserForTest UserFromFuture = UserForTest.builder()
            .id(104)
            .email("ya@ya.ru")
            .login("Terminator")
            .name("Name 104")
            .birthday(LocalDate.of(2222, 12, 10))
            .build();

    private final UserForTest User1 = UserForTest.builder()
            .email("partizan@ya.ru")
            .login("Login")
            .name("Valera")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();

    private final UserForTest User2 = UserForTest.builder()
            .id(1)
            .email("partizan@ya.ru")
            .login("Privet")
            .name("Ivan")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();

    @Test
    void checkAddUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noEmailUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Email не должен быть пустым]"));

        response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(badEmailFormatUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Email должен быть корректным]"));

        response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noLoginUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Логин не должен быть пустым]"));

        response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noLoginFillUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        message = response.getResponse().getContentAsString();
        assertTrue(message.equals("Логин не может содержать пробелы"));

        response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(UserFromFuture)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [День рожденья должен быть раньше текущей даты]"));

        response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(User1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        User returnedUser = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<User>() {
                }.getType());
        assertTrue(returnedUser.getName().equals("Valera"));

        response = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<User> returnedUsers = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<ArrayList<User>>() {
                }.getType());
        assertEquals(1, returnedUsers.size());

        response = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(User2)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        returnedUser = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<User>() {
                }.getType());
        assertEquals("Ivan", returnedUser.getName());

        response = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        returnedUsers = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<ArrayList<User>>() {
                }.getType());
        assertEquals(1, returnedUsers.size());

    }
}
