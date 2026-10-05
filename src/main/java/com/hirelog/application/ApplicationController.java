package com.hirelog.application;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hirelog.application.ApplicationDtos.ApplicationResponse;
import com.hirelog.application.ApplicationDtos.CreateApplicationRequest;
import com.hirelog.application.ApplicationDtos.StatusChangeRequest;
import com.hirelog.application.ApplicationDtos.StatusChangeResponse;
import com.hirelog.application.ApplicationDtos.UpdateApplicationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApplicationResponse> list(@RequestParam(required = false) ApplicationStatus status) {
        return service.list(status).stream().map(ApplicationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id) {
        return ApplicationResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody CreateApplicationRequest request) {
        return ApplicationResponse.from(service.create(request));
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateApplicationRequest request) {
        return ApplicationResponse.from(service.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return ApplicationResponse.from(service.changeStatus(id, request.status(), request.note()));
    }

    @GetMapping("/{id}/history")
    public List<StatusChangeResponse> history(@PathVariable Long id) {
        return service.history(id).stream().map(StatusChangeResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
