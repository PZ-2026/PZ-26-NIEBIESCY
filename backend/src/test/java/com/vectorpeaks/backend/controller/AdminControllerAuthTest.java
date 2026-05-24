/*
 * AdminControllerAuthTest.java
 *
 * Version: 1.1
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.LoginRequest;
import com.vectorpeaks.backend.entity.Role;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.*;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration authorization tests for AdminController endpoints.
 *
 * <p>Tests verify that each endpoint properly enforces @PreAuthorize constraints
 * for all four authentication states: unauthenticated, STUDENT, TUTOR, and ADMIN.
 *
 * <h2>Why real login instead of jwtUtil.generateToken directly?</h2>
 * <p>The previous version called jwtUtil.generateToken(..., "ROLE_ADMIN") directly.
 * This caused 403 for all admin tests because:
 * <ol>
 *   <li>JwtAuthenticationFilter loads the user from the database via UserDetailsService
 *       and builds authorities from user.getAuthorities() — not from the token claim.</li>
 *   <li>user.getAuthorities() calls user.getRole().getName() which queries the "roles"
 *       table. In the test DB the roles table had no rows → null authority → empty list
 *       → Spring Security treated the token as anonymous → 403.</li>
 * </ol>
 * <p>Using the real POST /api/auth/login endpoint guarantees:
 * <ul>
 *   <li>The token is issued through the same code path as production.</li>
 *   <li>The filter recognises and validates it identically.</li>
 *   <li>Any future changes to token structure are automatically picked up.</li>
 * </ul>
 *
 * <h2>Why @Sql("roles.sql")?</h2>
 * <p>The "roles" table is a static reference table (ADMIN=1, TUTOR=2, STUDENT=3).
 * It is not cleaned between tests (only the "users" table is reset in setUp), so it is
 * populated once per context via the SQL script rather than in @BeforeEach.
 *
 * <h2>NOTE — @AutoConfigureMockMvc(addFilters = false) is WRONG here</h2>
 * <p>Disabling servlet filters would bypass JwtAuthenticationFilter entirely. The
 * SecurityContextHolder would be empty, and @PreAuthorize would reject every request
 * with 403 regardless of the token — making every "shouldAllowAdmin" test still fail,
 * and making the "shouldReject*" tests pass by accident rather than by actual
 * security enforcement. Keep addFilters at its default (true).
 *
 * @version 1.1
 * @author EduLink Team
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc          // addFilters defaults to true — do NOT set it to false
@ActiveProfiles("test")
@DisplayName("AdminController Authorization Tests")
@Sql(scripts = "/test-data/roles.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AdminControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

//    @Autowired
//    private OfferRepository offerRepository;
//
//    @Autowired
//    private BookingRepository bookingRepository;
//
//    @Autowired
//    private ReviewRepository reviewRepository;
//
//    @Autowired
//    private AvailabilitySlotRepository slotRepository;
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    @Autowired
//    private ChatRepository chatRepository;
//
//    @Autowired
//    private SubjectRepository subjectRepository;
//
//    @Autowired
//    private RefreshTokenRepository refreshTokenRepository;

    /** Raw cleartext passwords — BCrypt hashes are stored in the DB. */
    private static final String STUDENT_PASSWORD = "studentPass1";
    private static final String TUTOR_PASSWORD   = "tutorPass1";
    private static final String ADMIN_PASSWORD   = "adminPass1";

    /** JWT Bearer tokens obtained by logging in via the real endpoint. */
    private String studentToken;
    private String tutorToken;
    private String adminToken;

    // -----------------------------------------------------------------------
    // Test setup / teardown
    // -----------------------------------------------------------------------

    /**
     * Creates three users (STUDENT/TUTOR/ADMIN) and logs each of them in
     * through the real POST /api/auth/login endpoint to obtain genuine JWT tokens.
     *
     * <p>This ensures the token format and authority extraction in
     * JwtAuthenticationFilter work exactly as in production — rather than
     * relying on a directly-constructed token that may use a different role prefix.
     */
    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        // Clean up tables that are reset between tests.
        // The "roles" table is static and was populated by @Sql above — do not touch it.
//        messageRepository.deleteAll();
//        chatRepository.deleteAll();
//        reviewRepository.deleteAll();
//        bookingRepository.deleteAll();
//        offerRepository.deleteAll();
//        subjectRepository.deleteAll();
//        slotRepository.deleteAll();
//        refreshTokenRepository.deleteAll();
//        userRepository.deleteAll();

        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

        studentToken = createUserAndLogin("student@test.com", STUDENT_PASSWORD, 3, enc);
        tutorToken   = createUserAndLogin("tutor@test.com",   TUTOR_PASSWORD,   2, enc);
        adminToken   = createUserAndLogin("admin@test.com",   ADMIN_PASSWORD,   1, enc);
    }

    /**
     * Saves a user to the DB and calls POST /api/auth/login to obtain a real JWT.
     *
     * @param email    the user's e-mail address
     * @param password cleartext password (will be hashed before saving)
     * @param roleId   1 = ADMIN, 2 = TUTOR, 3 = STUDENT
     * @param enc      BCrypt encoder
     * @return the JWT access token string extracted from the login response
     */
    private String createUserAndLogin(String email, String password,
                                      int roleId, BCryptPasswordEncoder enc)
            throws Exception {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword(enc.encode(password));   // hash stored in DB
        user.setRoleId(roleId);
        user.setAccountStatusId(1);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Flush and clear cash to avoid errors, let the hibernate dowlnoad data from db
        entityManager.flush();
        entityManager.clear();

        // Log in via the real endpoint — identical to what the Android client does.
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);       // cleartext sent in request body

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .with(req -> { req.setRemoteAddr("127.0.0.1"); return req; })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/stats
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/stats")
    class GetStatsEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/stats"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(0)))
                    .andExpect(jsonPath("$.totalOffers", greaterThanOrEqualTo(0)))
                    .andExpect(jsonPath("$.totalBookings", greaterThanOrEqualTo(0)));
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/bookings/pending
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/bookings/pending")
    class GetPendingBookingsEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/bookings/pending"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(get("/api/admin/bookings/pending")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(get("/api/admin/bookings/pending")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(get("/api/admin/bookings/pending")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/offers/pending
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/offers/pending")
    class GetPendingOffersEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/offers/pending"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(get("/api/admin/offers/pending")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(get("/api/admin/offers/pending")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(get("/api/admin/offers/pending")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // -----------------------------------------------------------------------
    // PUT /api/admin/offers/{id}/status
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/admin/offers/{id}/status")
    class UpdateOfferStatusEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(put("/api/admin/offers/1/status")
                            .param("status", "ACCEPTED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(put("/api/admin/offers/1/status")
                            .header("Authorization", "Bearer " + studentToken)
                            .param("status", "ACCEPTED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(put("/api/admin/offers/1/status")
                            .header("Authorization", "Bearer " + tutorToken)
                            .param("status", "ACCEPTED"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should pass security for ADMIN (404 expected since offer does not exist)")
        void shouldPassSecurityForAdmin() throws Exception {
            // Security passes → controller runs → offer not found → 404.
            // This confirms the request was NOT blocked by @PreAuthorize.
            mockMvc.perform(put("/api/admin/offers/99999/status")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("status", "ACCEPTED"))
                    .andExpect(status().isNotFound());
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/admin/subjects
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/admin/subjects")
    class PostSubjectsEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/admin/subjects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("name", "Mathematics"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(post("/api/admin/subjects")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("name", "Mathematics"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(post("/api/admin/subjects")
                            .header("Authorization", "Bearer " + tutorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("name", "Mathematics"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(post("/api/admin/subjects")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("name", "TestSubject_Auth"))))
                    .andExpect(status().isOk());
        }
    }

    // -----------------------------------------------------------------------
    // DELETE /api/admin/subjects/{id}
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/admin/subjects/{id}")
    class DeleteSubjectEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/admin/subjects/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(delete("/api/admin/subjects/1")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(delete("/api/admin/subjects/1")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should pass security for ADMIN (404 expected since subject does not exist)")
        void shouldPassSecurityForAdmin() throws Exception {
            mockMvc.perform(delete("/api/admin/subjects/99999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/reports
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/reports")
    class GetReportsEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/reports"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(get("/api/admin/reports")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(get("/api/admin/reports")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(get("/api/admin/reports")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/settings
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/settings")
    class GetSettingsEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/settings"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(get("/api/admin/settings")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(get("/api/admin/settings")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(get("/api/admin/settings")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
        }
    }

    // -----------------------------------------------------------------------
    // PUT /api/admin/settings
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/admin/settings")
    class UpdateSettingsEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(put("/api/admin/settings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("maxPricePerHour", 200.0, "globalMessage", ""))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(put("/api/admin/settings")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("maxPricePerHour", 200.0, "globalMessage", ""))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(put("/api/admin/settings")
                            .header("Authorization", "Bearer " + tutorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("maxPricePerHour", 200.0, "globalMessage", ""))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK when authenticated as ADMIN")
        void shouldAllowAdmin() throws Exception {
            mockMvc.perform(put("/api/admin/settings")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("maxPricePerHour", 150.0, "globalMessage", "Test message"))))
                    .andExpect(status().isOk());
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/reports/pdf
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/admin/reports/pdf")
    class GetPdfReportEndpoint {

        @Test
        @DisplayName("Should return 403 when unauthenticated")
        void shouldRejectUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/admin/reports/pdf"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as STUDENT")
        void shouldRejectStudent() throws Exception {
            mockMvc.perform(get("/api/admin/reports/pdf")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when authenticated as TUTOR")
        void shouldRejectTutor() throws Exception {
            mockMvc.perform(get("/api/admin/reports/pdf")
                            .header("Authorization", "Bearer " + tutorToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN reaches the endpoint (security passes — result depends on PDF library)")
        void shouldPassSecurityForAdmin() throws Exception {
            // We verify the response is NOT a security rejection (403/401).
            // The actual status (200 or 5xx) depends on the PDF library being
            // available in the test environment and is outside the scope of this test.
            mockMvc.perform(get("/api/admin/reports/pdf")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().is(not(equalTo(403))))
                    .andExpect(status().is(not(equalTo(401))));
        }
    }
}