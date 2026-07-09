package com.kostas.bookingproject.controllers;

import com.kostas.bookingproject.security.RequestResetDTO;
import com.kostas.bookingproject.security.ConfirmResetDTO;
import com.kostas.bookingproject.services.PasswordResetService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService resetService;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("/request-reset")
    public String requestReset(@RequestBody RequestResetDTO dto) {
        resetService.requestReset(dto.getEmail());
        return "Reset link sent";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ConfirmResetDTO dto) {
        resetService.confirmReset(dto.getToken(), dto.getNewPassword());
        return "Password updated";
    }
}
