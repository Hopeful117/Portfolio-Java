package com.hopefull117.portfolio.java.service;

import com.hopefull117.portfolio.java.exception.DateIntoFutureException;
import com.hopefull117.portfolio.java.exception.EntityNotFoundException;
import com.hopefull117.portfolio.java.model.TimelineEntry;
import com.hopefull117.portfolio.java.repository.TimelineEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TimelineEntryService extends AbstractCrudService<TimelineEntry> {
    private final TimelineEntryRepository timelineEntryRepository;

    protected TimelineEntryService(JpaRepository<TimelineEntry, Long> jpaRepository, TimelineEntryRepository timelineEntryRepository) {
        super(jpaRepository);
        this.timelineEntryRepository = timelineEntryRepository;
    }


    public List<TimelineEntry> getTimeline() {
        return timelineEntryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    public void update(Long id,TimelineEntry timelineEntry){
        TimelineEntry toUpdate = timelineEntryRepository.findById(id).orElseThrow(()->new EntityNotFoundException("TimelineEntry non trouvé"));
        if(timelineEntry.getDate()==null){
            timelineEntry.setDate(toUpdate.getDate());
        }

        if(timelineEntry.getDate().isAfter(LocalDate.now())){
            throw new DateIntoFutureException("La date est dans le future");
        }



        toUpdate.setTitle(timelineEntry.getTitle());
        toUpdate.setDescription(timelineEntry.getDescription());
        toUpdate.setDate(timelineEntry.getDate());
        toUpdate.setLink(timelineEntry.getLink());
        toUpdate.setDisplayOrder(timelineEntry.getDisplayOrder());
        toUpdate.setType(timelineEntry.getType());

        timelineEntryRepository.save(toUpdate);




    }
}
