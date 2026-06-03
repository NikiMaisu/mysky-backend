package ge.mysky.backend.controller;

import ge.mysky.backend.domain.WorkSchedule;
import ge.mysky.backend.dto.WorkScheduleDto;
import ge.mysky.backend.service.WorkScheduleService;
import jakarta.validation.Valid;
import java.time.LocalTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/work-schedule")
public class WorkScheduleController {

    private final WorkScheduleService service;

    public WorkScheduleController(WorkScheduleService service) {
        this.service = service;
    }

    @GetMapping
    public WorkScheduleDto get() {
        return toDto(service.getGlobal());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public WorkScheduleDto update(@Valid @RequestBody WorkScheduleDto req) {
        var saved = service.updateGlobal(req.days(), LocalTime.parse(req.start()), LocalTime.parse(req.end()));
        return toDto(saved);
    }

    static WorkScheduleDto toDto(WorkSchedule s) {
        return new WorkScheduleDto(
                WorkScheduleService.toDays(s.getWorkDays()),
                s.getStartTime().toString(),
                s.getEndTime().toString());
    }
}
