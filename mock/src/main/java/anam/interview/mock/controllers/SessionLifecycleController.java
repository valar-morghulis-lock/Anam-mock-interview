package anam.interview.mock.controllers;

import anam.interview.mock.service.SessionLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/interviews/{sessionId}")
@RequiredArgsConstructor
public class SessionLifecycleController {

    private final SessionLifecycleService sessionLifecycleService;

    @PostMapping("/start")
    public ResponseEntity<Void> start(@PathVariable UUID sessionId) {
        sessionLifecycleService.markStarted(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/end")
    public ResponseEntity<Void> end(@PathVariable UUID sessionId,
                                    @RequestParam(defaultValue = "true") boolean completedNaturally) {
        sessionLifecycleService.markEnded(sessionId, completedNaturally);
        return ResponseEntity.ok().build();
    }
}