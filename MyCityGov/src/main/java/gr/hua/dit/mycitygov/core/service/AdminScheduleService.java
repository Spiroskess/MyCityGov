package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.ServiceSchedule;

import java.util.List;

public interface AdminScheduleService {
    List<ServiceSchedule> findAll();
    ServiceSchedule create(ServiceSchedule schedule);
    void delete(Long id);
}
