package com.hirelog.followup;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hirelog.followup.FollowUpDtos.FollowUpRequest;
import com.hirelog.followup.FollowUpDtos.FollowUpResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/follow-ups")
public class FollowUpController {

    private final FollowUpService service;

    public FollowUpController(FollowUpService service) {
        this.service = service;
    }

    @GetMapping
    public List<FollowUpResponse> list(@RequestParam(required = false) Long applicationId,
            @RequestParam(defaultValue = "false") boolean pending) {
        return service.list(applicationId, pending).stream().map(FollowUpResponse::from).toList();
    }

    @GetMapping("/due")
    public List<FollowUpResponse> due() {
        return service.due().stream().map(FollowUpResponse::from).toList();
    }

    @GetMapping("/{id}")
    public FollowUpResponse get(@PathVariable Long id) {
        return FollowUpResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpResponse create(@Valid @RequestBody FollowUpRequest request) {
        return FollowUpResponse.from(service.create(request));
    }

    @PutMapping("/{id}")
    public FollowUpResponse update(@PathVariable Long id, @Valid @RequestBody FollowUpRequest request) {
        return FollowUpResponse.from(service.update(id, request));
    }

    @PostMapping("/{id}/complete")
    public FollowUpResponse complete(@PathVariable Long id) {
        return FollowUpResponse.from(service.complete(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
