    /*
     * UserControllerTest.java
     *
     * Version: 1.2
     * Date: 2026-05-03
     *
     * Copyright (c) 2026 EduLink Team. All rights reserved.
     *
     * This software is the confidential and proprietary information of EduLink.
     */

    package com.vectorpeaks.backend.controller;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.vectorpeaks.backend.dto.RegisterRequest;
    import com.vectorpeaks.backend.entity.User;
    import com.vectorpeaks.backend.repository.UserRepository;
    import com.vectorpeaks.backend.service.FcmTokenService;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.http.MediaType;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;

    import java.util.List;
    import java.util.Optional;

    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    /**
     * Unit tests for {@link UserController}.
     *
     * <p>Verifies the behaviour of the following endpoints:
     * <ul>
     *   <li>{@code GET /api/users} – retrieve all users,</li>
     *   <li>{@code POST /api/users} – add a new user,</li>
     *   <li>{@code GET /api/users/{id}} – retrieve a user by ID,</li>
     *   <li>{@code PUT /api/users/{id}} – update user profile,</li>
     *   <li>{@code DELETE /api/users/{id}} – anonymise a user account,</li>
     *   <li>{@code POST /api/users/register} – register a new account.</li>
     * </ul>
     *
     * <p>Uses {@code @WebMvcTest} with {@link MockMvc} – only the controller layer
     * is loaded; no full Spring context or database is required.
     * The {@link UserRepository} dependency is replaced by a Mockito mock
     * ({@code @MockitoBean}).
     *
     * @version 1.2
     * @author EduLink Team
     * @see UserController
     */
    @WebMvcTest(
            controllers = UserController.class,
            excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
    )
    class UserControllerTest extends BaseControllerTest {

        /** HTTP client used to perform requests in web-layer tests. */
        @Autowired
        private MockMvc mockMvc;

        /** Mock of the user repository – replaces the database layer. */
        @MockitoBean
        private UserRepository userRepository;

        /** JSON mapper used to serialize request objects. */
        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private FcmTokenService fcmTokenService;

        // -----------------------------------------------------------------------
        // GET /api/users
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns {@code 200 OK} and a JSON array
         * containing all users when records exist in the database.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void getAllUsers_listExists_returns200AndJsonArray() throws Exception {
            User u1 = buildUser(1, "Jan",  "Kowalski", "jan@example.com",  1);
            User u2 = buildUser(2, "Anna", "Nowak",    "anna@example.com", 2);

            when(userRepository.findAll()).thenReturn(List.of(u1, u2));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].firstName").value("Jan"))
                    .andExpect(jsonPath("$[1].firstName").value("Anna"));
        }

        /**
         * Verifies that the endpoint returns {@code 200 OK} and an empty JSON
         * array when no users exist in the database.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void getAllUsers_emptyList_returns200AndEmptyArray() throws Exception {
            when(userRepository.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        // -----------------------------------------------------------------------
        // POST /api/users
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns {@code 200 OK} and the saved user
         * data (with a generated ID) in JSON format.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void addUser_validUser_returns200AndSavedUser() throws Exception {
            User input = buildUser(null, "Piotr", "Wiśniewski", "piotr@example.com", 1);
            User saved  = buildUser(10,  "Piotr", "Wiśniewski", "piotr@example.com", 1);

            when(userRepository.save(any(User.class))).thenReturn(saved);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.firstName").value("Piotr"))
                    .andExpect(jsonPath("$.email").value("piotr@example.com"));
        }

        /**
         * Verifies that the response after saving a user contains the correct
         * {@code roleId} field matching the input data.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void addUser_savedData_containsRoleId() throws Exception {
            User saved = buildUser(5, "Ewa", "Zielinska", "ewa@example.com", 3);
            when(userRepository.save(any(User.class))).thenReturn(saved);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saved)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roleId").value(3));
        }

        // -----------------------------------------------------------------------
        // GET /api/users/{id}
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns {@code 200 OK} and the correct user
         * data when a user with the given ID exists.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void getUserById_userExists_returns200AndUser() throws Exception {
            User user = buildUser(7, "Marek", "Lis", "marek@example.com", 2);
            when(userRepository.findById(7)).thenReturn(Optional.of(user));

            mockMvc.perform(get("/api/users/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.firstName").value("Marek"));
        }

        /**
         * Verifies that the endpoint returns {@code 404 Not Found}
         * when no user with the given ID exists.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void getUserById_userNotFound_returns404() throws Exception {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());
        }

        // -----------------------------------------------------------------------
        // PUT /api/users/{id}
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns {@code 200 OK} and the updated user
         * data when a valid phone number and address are provided.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void updateUser_validData_returns200AndUpdatedUser() throws Exception {
            User existing = buildUser(3, "Jan", "Kowalski", "jan@example.com", 1);
            User updated  = buildUser(3, "Jan", "Kowalski", "jan@example.com", 1);
            updated.setAddress("Warszawa");
            updated.setPhoneNumber("123456789");

            when(userRepository.findById(3)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(updated);

            mockMvc.perform(put("/api/users/3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updated)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.address").value("Warszawa"))
                    .andExpect(jsonPath("$.phoneNumber").value("123456789"));
        }

        /**
         * Verifies that the endpoint returns {@code 400 Bad Request}
         * when the phone number does not consist of exactly 9 digits.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void updateUser_invalidPhone_returns400() throws Exception {
            User payload = buildUser(3, "Jan", "Kowalski", "jan@example.com", 1);
            payload.setPhoneNumber("123");

            mockMvc.perform(put("/api/users/3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns {@code 400 Bad Request}
         * when the address is shorter than 2 characters.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void updateUser_addressTooShort_returns400() throws Exception {
            User payload = buildUser(3, "Jan", "Kowalski", "jan@example.com", 1);
            payload.setAddress("X");

            mockMvc.perform(put("/api/users/3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns {@code 404 Not Found}
         * when the user to be updated does not exist.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void updateUser_userNotFound_returns404() throws Exception {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            User payload = buildUser(999, "X", "Y", "x@example.com", 1);
            payload.setAddress("Kraków");

            mockMvc.perform(put("/api/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isNotFound());
        }

        // -----------------------------------------------------------------------
        // DELETE /api/users/{id}
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns {@code 200 OK}
         * when a user is successfully anonymised.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void deleteUser_userExists_returns200() throws Exception {
            User user = buildUser(5, "Ewa", "Nowak", "ewa@example.com", 2);
            when(userRepository.findById(5)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            mockMvc.perform(delete("/api/users/5"))
                    .andExpect(status().isOk());
        }

        /**
         * Verifies that the endpoint returns {@code 404 Not Found}
         * when the user to be deleted does not exist.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void deleteUser_userNotFound_returns404() throws Exception {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/users/999"))
                    .andExpect(status().isNotFound());
        }

        // -----------------------------------------------------------------------
        // POST /api/users/register
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns {@code 200 OK}
         * when all required registration fields are valid and the email is unique.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void register_validRequest_returns200() throws Exception {
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(new User());

            RegisterRequest req = buildRegisterRequest(
                    "Tomasz", "Górski", "new@example.com", "haslo123", 2, "Kraków", "987654321");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        /**
         * Verifies that the endpoint returns {@code 400 Bad Request}
         * when the email address is already in use.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void register_duplicateEmail_returns400() throws Exception {
            User existing = buildUser(1, "Anna", "Nowak", "existing@example.com", 2);
            when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

            RegisterRequest req = buildRegisterRequest(
                    "Anna", "Nowak", "existing@example.com", "haslo123", 2, "Gdańsk", "111222333");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns {@code 400 Bad Request}
         * when the email address format is invalid.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void register_invalidEmail_returns400() throws Exception {
            RegisterRequest req = buildRegisterRequest(
                    "Anna", "Nowak", "not-an-email", "haslo123", 2, "Gdańsk", "111222333");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns {@code 400 Bad Request}
         * when the role ID is not 2 or 3.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void register_invalidRole_returns400() throws Exception {
            when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

            RegisterRequest req = buildRegisterRequest(
                    "Anna", "Nowak", "new@example.com", "haslo123", 99, "Gdańsk", "111222333");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns {@code 400 Bad Request}
         * when a required field (e.g. first name) is missing.
         *
         * @throws Exception if the MockMvc request execution fails
         */
        @Test
        void register_missingFirstName_returns400() throws Exception {
            RegisterRequest req = buildRegisterRequest(
                    "", "Nowak", "new@example.com", "haslo123", 2, "Gdańsk", "111222333");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        // -----------------------------------------------------------------------
        // Helpers
        // -----------------------------------------------------------------------

        /**
         * Creates a {@link User} object with the given data – helper method
         * used to build test fixtures.
         *
         * @param id      user identifier ({@code null} before persistence)
         * @param first   first name
         * @param last    last name
         * @param email   e-mail address
         * @param roleId  role identifier
         * @return new {@link User} populated with the provided data
         */
        private User buildUser(Integer id, String first, String last,
                               String email, Integer roleId) {
            User u = new User();
            u.setId(id);
            u.setFirstName(first);
            u.setLastName(last);
            u.setEmail(email);
            u.setRoleId(roleId);
            return u;
        }

        /**
         * Creates a {@link RegisterRequest} with the given data – helper method
         * used to build registration test fixtures.
         *
         * @param firstName   first name
         * @param lastName    last name
         * @param email       e-mail address
         * @param password    plain-text password
         * @param roleId      role identifier
         * @param city        city
         * @param phoneNumber phone number
         * @return new {@link RegisterRequest} populated with the provided data
         */
        private RegisterRequest buildRegisterRequest(String firstName, String lastName,
                                                     String email, String password,
                                                     Integer roleId, String city,
                                                     String phoneNumber) {
            RegisterRequest r = new RegisterRequest();
            r.setFirstName(firstName);
            r.setLastName(lastName);
            r.setEmail(email);
            r.setPassword(password);
            r.setRoleId(roleId);
            r.setCity(city);
            r.setPhoneNumber(phoneNumber);
            return r;
        }

        // -----------------------------------------------------------------------
        // PUT /api/users/{id}/status
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns 200 OK and updates the user status
         * when valid data is provided.
         */
        @Test
        void updateUserStatus_validData_returns200() throws Exception {
            User user = buildUser(3, "Jan", "Kowalski", "jan@example.com", 1);
            user.setAccountStatusId(1); // Old status

            when(userRepository.findById(3)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            mockMvc.perform(put("/api/users/3/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountStatusId\": 9}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountStatusId").value(9));
        }

        /**
         * Verifies that the endpoint returns 400 Bad Request when the status ID is missing.
         */
        @Test
        void updateUserStatus_missingStatusId_returns400() throws Exception {
            mockMvc.perform(put("/api/users/3/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns 404 Not Found when the user doesn't exist.
         */
        @Test
        void updateUserStatus_userNotFound_returns404() throws Exception {
            when(userRepository.findById(999)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/users/999/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accountStatusId\": 9}"))
                    .andExpect(status().isNotFound());
        }

        // -----------------------------------------------------------------------
        // POST /api/users/{userId}/fcm-token
        // -----------------------------------------------------------------------

        /**
         * Verifies that the endpoint returns 200 OK when registering a valid FCM token.
         */
        @Test
        void registerFcmToken_validRequest_returns200() throws Exception {
            when(userRepository.existsById(5)).thenReturn(true);

            mockMvc.perform(post("/api/users/5/fcm-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fcmToken\": \"dummy-fcm-token-123\"}"))
                    .andExpect(status().isOk());
        }

        /**
         * Verifies that the endpoint returns 400 Bad Request when the FCM token is missing.
         */
        @Test
        void registerFcmToken_missingToken_returns400() throws Exception {
            when(userRepository.existsById(5)).thenReturn(true);

            mockMvc.perform(post("/api/users/5/fcm-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fcmToken\": \"\"}"))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that the endpoint returns 404 Not Found when attempting to add
         * a token for a non-existent user.
         */
        @Test
        void registerFcmToken_userNotFound_returns404() throws Exception {
            when(userRepository.existsById(999)).thenReturn(false);

            mockMvc.perform(post("/api/users/999/fcm-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fcmToken\": \"dummy-fcm-token-123\"}"))
                    .andExpect(status().isNotFound());
        }
    }