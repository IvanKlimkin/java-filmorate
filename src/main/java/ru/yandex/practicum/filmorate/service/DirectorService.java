package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director createDirector(Director director) {
        director = directorStorage.addDirector(director);
        return director;
    }

    public Director updateDirector(Director director) {
        getDirectorById(director.getId());
        directorStorage.updateDirector(director);
        return director;
    }

    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(Integer id) {
        return directorStorage.getDirectorById(id);
    }

    public void deleteDirector(Integer Id) {
        directorStorage.deleteDirector(Id);
    }

}
