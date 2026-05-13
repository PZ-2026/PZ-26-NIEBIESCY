-- 1. Roles
INSERT INTO roles (id, name) VALUES 
(1, 'Admin'), (2, 'Tutor'), (3, 'Student');

-- 2. Statuses
INSERT INTO statuses (id, status) VALUES 
(1, 'Active'), (2, 'Inactive'), (3, 'Pending'), (4, 'Completed'), 
(5, 'Cancelled'), (6, 'Accepted'), (7, 'Rejected'), (8, 'Suspended'), (9, 'Deleted');

-- 3. GlobalLimits
INSERT INTO global_limits (id, hourly_price_limit, message) VALUES 
(1, 200.00, 'Tutor hour price limit');

-- 4. AvailabilitySlots
INSERT INTO availability_slots (id, day_of_week, start_time, end_time) VALUES 
(1, 1, '08:00:00', '09:00:00'), (2, 1, '10:00:00', '11:00:00'),
(3, 2, '14:00:00', '15:00:00'), (4, 3, '16:00:00', '17:00:00'),
(5, 4, '09:00:00', '10:00:00'), (6, 5, '12:00:00', '13:00:00'),
(7, 6, '10:00:00', '12:00:00'), (8, 0, '18:00:00', '20:00:00');

-- 5. Subjects
INSERT INTO subjects (id, name, status_id) VALUES 
(1, 'Matematyka', 1), (2, 'Fizyka', 1), (3, 'J.Angielski', 1), 
(4, 'Biologia', 1), (5, 'Chemia', 1), (6, 'Historia', 1), 
(7, 'Informatyka', 1), (8, 'Geografia', 1);

-- 6. Users
INSERT INTO users (id, role_id, password, first_name, last_name, email, account_status_id, address, phone_number, fcm_token) VALUES 
(1, 1, 'hash1', 'Jan',    'Kowalski',    'admin@edulink.com',   1, 'Warszawa', '111222333', NULL),
(2, 2, 'hash2', 'Anna',   'Nowak',       'tutor@edulink.com',   1, 'Kraków',   '222333444', NULL),
(3, 3, 'hash3', 'Piotr',  'Zieliński',   'student@edulink.com', 1, 'Gdańsk',   '333444555', NULL),
(4, 3, 'hash4', 'Maria',  'Dąbrowska',   'maria@edulink.com',   1, 'Wrocław',  '444555666', NULL),
(5, 2, 'hash5', 'Robert', 'Lewandowski', 'robert@edulink.com',  1, 'Poznań',   '555666777', NULL),
(6, 3, 'hash6', 'Kasia',  'Wójcik',      'kasia@edulink.com',   1, 'Łódź',     '666777888', NULL),
(7, 3, 'hash7', 'Michał', 'Wiśniewski',  'michal@edulink.com',  2, 'Szczecin', '777888999', NULL),
(8, 2, 'hash8', 'Ewa',    'Kozłowska',   'ewa@edulink.com',     1, 'Lublin',   '888999000', NULL),
(9, 3, 'hash9', 'Tomasz', 'Jankowski',   'tomek@edulink.com',   1, 'Białystok','999000111', NULL),
(10,3, 'hash10','Adam',   'Mickiewicz',  'adam@edulink.com',    1, 'Wilno',    '000111222', NULL);

-- 7. Offers
INSERT INTO offers (id, tutor_id, price, availability_slot_id, details, subject_id, status_id, global_limit_id, offer_type) VALUES 
(1, 1, 60.00, 1, 'Podstawy algebry', 1, 1, 1, 'Stacjonarne'),
(2, 1, 70.00, 2, 'Analiza matematyczna', 1, 1, 1, 'Online'),
(3, 2, 50.00, 3, 'Gramatyka i czasy w j. Angielskim', 3, 1, 1, 'Stacjonarne'),
(4, 5, 80.00, 4, 'Fizyka kwantowa i teoria strun', 2, 1, 1, 'Online'),
(5, 8, 45.00, 5, 'Historia 2 wojny światowej', 6, 1, 1, 'Stacjonarne'),
(6, 2, 55.00, 6, 'Angielski Zawodowy', 3, 1, 1, 'Stacjonarne'),
(7, 1, 65.00, 7, 'Python dla początkujących', 7, 1, 1, 'Stacjonarne'),
(8, 5, 90.00, 8, 'Fizyka podstawowa', 2, 1, 1, 'Online');

-- 8. Bookings
INSERT INTO bookings (id, availability_slot_id, status_id, offer_id, student_id, booking_date) VALUES 
(1, 1, 4, 1, 3, '2023-10-01 10:00:00'),
(2, 2, 4, 2, 4, '2023-10-02 11:00:00'),
(3, 3, 6, 3, 6, '2023-10-03 14:00:00'),
(4, 4, 1, 4, 9, '2023-10-04 16:00:00'),
(5, 5, 7, 5, 10, '2023-10-05 09:00:00'),
(6, 6, 4, 6, 3, '2023-10-06 12:00:00'),
(7, 7, 4, 7, 4, '2023-10-07 10:00:00'),
(8, 8, 3, 8, 6, '2023-10-08 18:00:00');

-- 9. Reviews
INSERT INTO reviews (id, rating, tutor_id, comment, booking_id) VALUES 
(1, 5, 1, 'Bardzo dobry nauczyciel!', 1),
(2, 4, 1, 'Bardzo pomocny.', 2),
(3, 5, 2, 'Excellent English lesson.', 6),
(4, 3, 8, 'Trochę przynudza, ale uczy dobrze.', 5),
(5, 5, 1, 'Dobrze tłumaczy zawiłości w programowaniu.', 7),
(6, 2, 5, 'Zbyt skomplikowane dla mnie.', 4),
(7, 5, 2, 'Super friendly!', 3),
(8, 4, 5, 'Bardzo kompetentny.', 8);

-- 10. Chats
INSERT INTO chats (id, created_at) VALUES 
(1, '2023-09-20 12:00:00'), (2, '2023-09-21 13:00:00'), 
(3, '2023-09-22 14:00:00'), (4, '2023-09-23 15:00:00'),
(5, '2023-09-24 16:00:00'), (6, '2023-09-25 17:00:00'),
(7, '2023-09-26 18:00:00'), (8, '2023-09-27 19:00:00');

-- 11. ChatParticipants
INSERT INTO chat_participants (chat_id, user_id) VALUES 
(1, 1), (1, 3), (2, 1), (2, 4), (3, 2), (3, 6), (4, 5), (4, 9),
(5, 8), (5, 10), (6, 2), (6, 3), (7, 1), (7, 4), (8, 5), (8, 6);

-- 12. Messages
INSERT INTO messages (id, chat_id, content, sent_at, user_id) VALUES 
(1, 1, 'Hello, I want to book a lesson', '2023-09-20 12:05:00', 3),
(2, 1, 'Sure, what time?', '2023-09-20 12:10:00', 1),
(3, 2, 'Is the offer still active?', '2023-09-21 13:05:00', 4),
(4, 3, 'I will be 5 minutes late', '2023-09-22 14:05:00', 6),
(5, 4, 'Can we move to Zoom?', '2023-09-23 15:05:00', 5),
(6, 5, 'Thanks for the history lesson!', '2023-09-24 16:05:00', 10),
(7, 6, 'Do you have materials for B2?', '2023-09-25 17:05:00', 3),
(8, 7, 'Yes, check your email.', '2023-09-26 18:05:00', 1);

SELECT pg_get_serial_sequence('bookings', 'id');
-- Aktualizacja sekwencji dla wszystkich tabel z autoinkrementacją
SELECT setval(pg_get_serial_sequence('roles', 'id'), (SELECT MAX(id) FROM roles));
SELECT setval(pg_get_serial_sequence('statuses', 'id'), (SELECT MAX(id) FROM statuses));
SELECT setval(pg_get_serial_sequence('global_limits', 'id'), (SELECT MAX(id) FROM global_limits));
SELECT setval(pg_get_serial_sequence('availability_slots', 'id'), (SELECT MAX(id) FROM availability_slots));
SELECT setval(pg_get_serial_sequence('subjects', 'id'), (SELECT MAX(id) FROM subjects));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('offers', 'id'), (SELECT MAX(id) FROM offers));
SELECT setval(pg_get_serial_sequence('bookings', 'id'), (SELECT MAX(id) FROM bookings));
SELECT setval(pg_get_serial_sequence('reviews', 'id'), (SELECT MAX(id) FROM reviews));
SELECT setval(pg_get_serial_sequence('chats', 'id'), (SELECT MAX(id) FROM chats));
SELECT setval(pg_get_serial_sequence('messages', 'id'), (SELECT MAX(id) FROM messages));

-- Hash passwords
CREATE EXTENSION IF NOT EXISTS pgcrypto;
UPDATE users SET password = crypt(password, gen_salt('bf')) WHERE password NOT LIKE '$2a$%';
