package ru.yandex.practicum.filmorate;

/*
@WebMvcTest(controllers = UserController.class)
class UserControllerTests {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
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
    private final UserForTest userFromFuture = UserForTest.builder()
            .id(104)
            .email("ya@ya.ru")
            .login("Terminator")
            .name("Name 104")
            .birthday(LocalDate.of(2222, 12, 10))
            .build();
    private final UserForTest user1 = UserForTest.builder()
            .email("partizan@ya.ru")
            .login("Login")
            .name("Valera")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();
    private final UserForTest user2 = UserForTest.builder()
            .id(1)
            .email("partizan@ya.ru")
            .login("Privet")
            .name("Ivan")
            .birthday(LocalDate.of(1990, 12, 10))
            .build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FilmService filmService;
    @Autowired
    private FilmStorage filmStorage;

    @Test
    @DisplayName("Тест добавления не корректного пользователя без почты")
    void checkAddNoEmailUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noEmailUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Email не должен быть пустым]"));
    }

    @Test
    @DisplayName("Тест добавления не корректного пользователя с не корректным форматом почты")
    void checkAddBadEmailFormatUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(badEmailFormatUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Email должен быть корректным]"));
    }

    @Test
    @DisplayName("Тест добавления не корректного фильма по полю Login(пустой)")
    void checkAddNoLoginUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noLoginUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Логин не должен быть пустым]"));
    }

    @Test
    @DisplayName("Тест добавления не корректного фильма по полю Login(с пробелами)")
    void checkAddNoLoginFillUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noLoginFillUser)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResponse().getContentAsString();
        assertTrue(message.equals("Логин не может содержать пробелы"));
    }

    @Test
    @DisplayName("Тест добавления не корректного пользователя с датой из будущего")
    void checkAddUserFromFuture() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(userFromFuture)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [День рожденья должен быть раньше текущей даты]"));
    }

    @Test
    @DisplayName("Тест добавления корректного пользователя и сравнение по запросу GET")
    void checkAddAndGetGoodUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user1)))
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
    }

    @Test
    @DisplayName("Тест обновления корректного пользователя и сравнение по запросу GET")
    void checkUpdateAndGetGoodUser() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(user2)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        User returnedUser = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<User>() {
                }.getType());
        assertEquals("Ivan", returnedUser.getName());

        response = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<User> returnedUsers = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<ArrayList<User>>() {
                }.getType());
        assertEquals(1, returnedUsers.size());

    }
}
*/