package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private  final MpaStorage mpaStorage;


    public List<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa getMpa(Integer id) {
        return mpaStorage.getMpa(id);
    }

}
