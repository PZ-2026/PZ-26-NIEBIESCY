package com.vectorpeaks.backend.security;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import com.vectorpeaks.backend.service.MaintenanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AccessInterceptor implements HandlerInterceptor {

    private final MaintenanceService maintenanceService;
    private final UserRepository userRepository;

    public AccessInterceptor(MaintenanceService maintenanceService, UserRepository userRepository) {
        this.maintenanceService = maintenanceService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // 1. Sprawdzenie trybu serwisowego
            if (maintenanceService.isFullyActive() && !isAdmin) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"Trwają prace serwisowe.\"}");
                return false;
            }

            // 2. Sprawdzenie zablokowania użytkownika
            Object principal = auth.getPrincipal();
            if (principal instanceof Integer) {
                Integer userId = (Integer) principal;
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isEmpty() || userOpt.get().getAccountStatusId() == null || userOpt.get().getAccountStatusId() != 1) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\": \"Konto zostało zablokowane.\"}");
                    return false;
                }
            }
        }

        return true;
    }
}
