package ru.yandex.practicum.filmorate;

/*
@WebMvcTest(controllers = FilmController.class)
@SpringBootTest
class FilmControllerTests {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    private final FilmForTest noNameFilm = FilmForTest.builder()
            .id(1)
            .name("")
            .description("Film has no name")
            .releaseDate(LocalDate.of(1997, 12, 10))
            .duration(120)
            .build();
    private final FilmForTest longDescriptionFilm = FilmForTest.builder()
            .id(2)
            .name("Long film")
            .description("Film has very loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "descption")
            .releaseDate(LocalDate.of(1997, 12, 10))
            .duration(120)
            .build();
    private final FilmForTest veryOldFilm = FilmForTest.builder()
            .id(3)
            .name(" Year 1885")
            .description("Film of 1885 year")
            .releaseDate(LocalDate.of(1885, 12, 10))
            .duration(120)
            .build();
    private final FilmForTest negativeDurationFilm = FilmForTest.builder()
            .id(4)
            .name("Film Negative")
            .description("Film has negative duration")
            .releaseDate(LocalDate.of(1997, 12, 10))
            .duration(-10)
            .build();
    private final FilmForTest Film1 = FilmForTest.builder()
            .name("Film 1")
            .description("Film 1 added")
            .releaseDate(LocalDate.of(1997, 12, 10))
            .duration(120)
            .build();
    private final FilmForTest Film2 = FilmForTest.builder()
            .id(1)
            .name("Film 1")
            .description("Film 2 replace Film 1")
            .releaseDate(LocalDate.of(1997, 12, 10))
            .duration(120)
            .build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FilmService filmService;
    @Autowired
    private FilmStorage filmStorage;

    @Test
    @DisplayName("Тест добавления не корректного фильма по полю Name")
    void checkAddFilm() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(noNameFilm)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("default message [Необходимо задать имя]"));
    }

    @Test
    @DisplayName("Тест добавления не корректного фильма с очень длинным описанием(более 200 символов)")
    void checkAddLongDescriptionFilm() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(longDescriptionFilm)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResponse().getContentAsString();
        assertTrue(message.equals("Слишком длинное описание фильма"));
    }

    @Test
    @DisplayName("Тест добавления не корректного фильма по полю Date")
    void checkAddVeryOldFilm() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(veryOldFilm)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResponse().getContentAsString();
        assertTrue(message.equals("Дата релиза раньше 28 декабря 1895 года"));
    }

    @Test
    @DisplayName("Тест добавления не корректного фильма с отрицательной длительностью")
    void checkAddnegativeDurationFilm() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(negativeDurationFilm)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String message = response.getResolvedException().getMessage();
        assertTrue(message.contains("Продолжительность должна быть положительной"));
    }

    @Test
    @DisplayName("Тест добавления корректного фильма и сравнение по запросу GET")
    void checkAddAndGetGoodFilm() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(Film1)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Film returnedFilm = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<Film>() {
                }.getType());
        assertTrue(returnedFilm.getName().equals("Film 1"));
        assertTrue(returnedFilm.getDescription().equals("Film 1 added"));

        response = mockMvc.perform(MockMvcRequestBuilders.get("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        List<Film> returnedFilms = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<ArrayList<Film>>() {
                }.getType());
        assertTrue(returnedFilms.size() == 1);
    }

    @Test
    @DisplayName("Тест обновления корректного фильма и сравнение по запросу GET")
    void checkUpdateFilm() throws Exception {
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(Film2)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Film returnedFilm = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<Film>() {
                }.getType());
        assertTrue(returnedFilm.getName().equals("Film 1"));
        assertTrue(returnedFilm.getDescription().equals("Film 2 replace Film 1"));

        response = mockMvc.perform(MockMvcRequestBuilders.get("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        List<Film> returnedFilms = gson.fromJson(response.getResponse().getContentAsString(),
                new TypeToken<ArrayList<Film>>() {
                }.getType());
        assertTrue(returnedFilms.size() == 1);
    }
}



*/