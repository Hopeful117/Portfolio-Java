package com.hopefull117.portfolio.java.service;

import java.util.List;

public interface CrudService<M> {
    List<M> getAll();
    M findById(Long id);
    void create(M model);
    void update (Long id,M model);
    void deleteById(Long id);

}
