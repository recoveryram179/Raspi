package com.hawks.raspi.controller;

import com.hawks.raspi.Constants;
import com.hawks.raspi.helpers.IpAddress;
import com.hawks.raspi.tcp.TcpServerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
@RequestMapping("tcp")
public class TcpController {

    private final TcpServerService tcpServerService;

    public TcpController(TcpServerService tcpServerService) {
        this.tcpServerService = tcpServerService;
    }

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    @GetMapping("/ui")
    public ModelAndView index(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("tcp");
        mav.addObject("base_url", IpAddress.getWebSocket(request));
        return mav;
    }

    // -------------------------------------------------------------------------
    // REST API
    // -------------------------------------------------------------------------

    /**
     * Send a message to the connected TCP client.
     * POST /tcp/send?message=hello
     */
    @PostMapping("send")
    public ResponseEntity<Map<String, Object>> send(@RequestParam String message) {
        if (message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Message must not be empty."));
        }

        try {
            tcpServerService.sendMessage(message.trim());
            return ResponseEntity.ok(Map.of("success", true, "message", message.trim()));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Check whether a TCP client is currently connected.
     * GET /tcp/status
     */
    @GetMapping("status")
    public ResponseEntity<Map<String, Object>> status() {
        boolean connected = tcpServerService.isClientConnected();
        return ResponseEntity.ok(Map.of(
                "tcpClientConnected", connected,
                "status", connected ? "connected" : "disconnected"
        ));
    }
}