package com.hopefull117.portfolio.java.service;


import com.hopefull117.portfolio.java.exception.EntityNotFoundException;
import com.hopefull117.portfolio.java.model.ModelEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public abstract class AbstractCrudService<M extends ModelEntity> implements CrudService<M>{
    protected final JpaRepository<M,Long> jpaRepository;
    protected AbstractCrudService(JpaRepository<M,Long> jpaRepository){
        this.jpaRepository=jpaRepository;
    }

    @Override
    public List<M>getAll(){
       return jpaRepository.findAll();
    }

    @Override
    public M findById(Long id){
        return jpaRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("Entité non trouvé"));
    }

    @Override
    public void create (M model){
        jpaRepository.save(model);
    }

    @Override
    public void update(Long id,M model){
        if(!jpaRepository.existsById(id)){
            throw new EntityNotFoundException("Entité non trouvée");
        }
        jpaRepository.save(model);

    }

    @Override
    public void deleteById(Long id){
        if(!jpaRepository.existsById(id)){
            throw new EntityNotFoundException("Entité non trouvée");
        }
        jpaRepository.deleteById(id);
    }

}
