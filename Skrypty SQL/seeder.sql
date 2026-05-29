-- 1. Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ADMIN'), (2, 'TUTOR'), (3, 'STUDENT');

-- 2. Statuses
INSERT INTO statuses (id, status) VALUES 
(1, 'Active'), (2, 'Inactive'), (3, 'Pending'), (4, 'Completed'), 
(5, 'Cancelled'), (6, 'Accepted'), (7, 'Rejected'), (8, 'Suspended'), (9, 'Deleted');

-- 3. GlobalLimits
INSERT INTO global_limits (id, hourly_price_limit, message) VALUES 
(1, 200.00, 'Tutor hour price limit');

-- 4. AvailabilitySlots
DO $$
DECLARE
    day_num INTEGER;
    hour_num INTEGER;
    next_id INTEGER := 1;
BEGIN
    FOR day_num IN 0..6 LOOP
        FOR hour_num IN 8..17 LOOP
            INSERT INTO availability_slots (id, day_of_week, start_time, end_time)
            VALUES (next_id, day_num, make_time(hour_num, 0, 0), make_time(hour_num+1, 0, 0));
            next_id := next_id + 1;
        END LOOP;
    END LOOP;
END $$;

-- 5. Subjects
INSERT INTO subjects (id, name, status_id) VALUES 
(1, 'Matematyka', 1), (2, 'Fizyka', 1), (3, 'J.Angielski', 1), 
(4, 'Biologia', 1), (5, 'Chemia', 1), (6, 'Historia', 1), 
(7, 'Informatyka', 1), (8, 'Geografia', 1);

-- 6. Users
INSERT INTO users (id, role_id, password, first_name, last_name, email, account_status_id, address, phone_number, fcm_token, created_at) VALUES 
(1,  1, 'hash1',  'Jan',    'Kowalski',    'admin@edulink.com',   1, 'Warszawa',  '111222333', NULL, '2026-05-01 08:00:00'),
(2,  2, 'hash2',  'Anna',   'Nowak',       'tutor@edulink.com',   1, 'Kraków',    '222333444', NULL, '2026-05-01 09:00:00'),
(3,  3, 'hash3',  'Piotr',  'Zieliński',   'student@edulink.com', 1, 'Gdańsk',    '333444555', NULL, '2026-05-02 10:00:00'),
(4,  3, 'hash4',  'Maria',  'Dąbrowska',   'maria@edulink.com',   1, 'Wrocław',   '444555666', NULL, '2026-05-02 11:00:00'),
(5,  2, 'hash5',  'Robert', 'Lewandowski', 'robert@edulink.com',  1, 'Poznań',    '555666777', NULL, '2026-05-03 12:00:00'),
(6,  3, 'hash6',  'Kasia',  'Wójcik',      'kasia@edulink.com',   1, 'Łódź',      '666777888', NULL, '2026-05-03 13:00:00'),
(7,  3, 'hash7',  'Michał', 'Wiśniewski',  'michal@edulink.com',  2, 'Szczecin',  '777888999', NULL, '2026-05-04 14:00:00'),
(8,  2, 'hash8',  'Ewa',    'Kozłowska',   'ewa@edulink.com',     1, 'Lublin',    '888999000', NULL, '2026-05-04 15:00:00'),
(9,  3, 'hash9',  'Tomasz', 'Jankowski',   'tomek@edulink.com',   1, 'Białystok', '999000111', NULL, '2026-05-05 16:00:00'),
(10, 3, 'hash10', 'Adam',   'Mickiewicz',  'adam@edulink.com',    1, 'Wilno',     '000111222', NULL, '2026-05-05 17:00:00');

-- 7. Offers
INSERT INTO offers (id, tutor_id, price, details, subject_id, status_id, global_limit_id, offer_type, created_at) VALUES 
(1, 1, 60.00, 'Podstawy algebry',                  1, 1, 1, 'Stacjonarne', '2026-05-05 10:00:00'),
(2, 1, 70.00, 'Analiza matematyczna',              1, 1, 1, 'Online',       '2026-05-05 11:00:00'),
(3, 2, 50.00, 'Gramatyka i czasy w j. Angielskim', 3, 1, 1, 'Stacjonarne', '2026-05-06 12:00:00'),
(4, 5, 80.00, 'Fizyka kwantowa i teoria strun',    2, 1, 1, 'Online',       '2026-05-06 13:00:00'),
(5, 8, 45.00, 'Historia 2 wojny światowej',        6, 1, 1, 'Stacjonarne', '2026-05-07 14:00:00'),
(6, 2, 55.00, 'Angielski Zawodowy',                3, 1, 1, 'Stacjonarne', '2026-05-07 15:00:00'),
(7, 1, 65.00, 'Python dla początkujących',         7, 1, 1, 'Stacjonarne', '2026-05-08 16:00:00'),
(8, 5, 90.00, 'Fizyka podstawowa',                 2, 1, 1, 'Online',       '2026-05-08 17:00:00');

-- 8. Offer_slots
INSERT INTO offer_slots (offer_id, availability_slot_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4),
(5, 5), (6, 6), (7, 7), (8, 8);

-- 9. Bookings
INSERT INTO bookings (id, availability_slot_id, status_id, offer_id, student_id, booking_date) VALUES 
(1, 1, 4, 1, 3,  '2026-05-08 10:00:00'),
(2, 2, 4, 2, 4,  '2026-05-09 11:00:00'),
(3, 3, 6, 3, 6,  '2026-05-10 14:00:00'),
(4, 4, 1, 4, 9,  '2026-05-10 16:00:00'),
(5, 5, 7, 5, 10, '2026-05-11 09:00:00'),
(6, 6, 4, 6, 3,  '2026-05-12 12:00:00'),
(7, 7, 4, 7, 4,  '2026-05-13 10:00:00'),
(8, 8, 3, 8, 6,  '2026-05-14 18:00:00');

-- 10. Reviews (z created_at i updated_at)
INSERT INTO reviews (id, rating, tutor_id, comment, booking_id, created_at, updated_at) VALUES 
(1, 5, 1, 'Bardzo dobry nauczyciel!',                     1, '2026-05-09 11:00:00', '2026-05-09 11:00:00'),
(2, 4, 1, 'Bardzo pomocny.',                              2, '2026-05-10 12:00:00', '2026-05-10 12:00:00'),
(3, 5, 2, 'Excellent English lesson.',                    6, '2026-05-13 09:00:00', '2026-05-13 09:00:00'),
(4, 3, 8, 'Trochę przynudza, ale uczy dobrze.',          5, '2026-05-12 10:00:00', '2026-05-14 15:00:00'),
(5, 5, 1, 'Dobrze tłumaczy zawiłości w programowaniu.', 7, '2026-05-14 11:00:00', '2026-05-14 11:00:00'),
(6, 2, 5, 'Zbyt skomplikowane dla mnie.',                 4, '2026-05-11 13:00:00', '2026-05-13 08:00:00'),
(7, 5, 2, 'Super friendly!',                              3, '2026-05-11 16:00:00', '2026-05-11 16:00:00'),
(8, 4, 5, 'Bardzo kompetentny.',                          8, '2026-05-15 10:00:00', '2026-05-15 10:00:00');

-- 11. Chats
INSERT INTO chats (id, created_at) VALUES 
(1, '2026-05-08 12:00:00'), (2, '2026-05-09 13:00:00'),
(3, '2026-05-10 14:00:00'), (4, '2026-05-10 15:00:00'),
(5, '2026-05-11 16:00:00'), (6, '2026-05-12 17:00:00'),
(7, '2026-05-13 18:00:00'), (8, '2026-05-14 19:00:00');

-- 12. ChatParticipants
INSERT INTO chat_participants (chat_id, user_id) VALUES 
(1, 1), (1, 3), (2, 1), (2, 4), (3, 2), (3, 6), (4, 5), (4, 9),
(5, 8), (5, 10), (6, 2), (6, 3), (7, 1), (7, 4), (8, 5), (8, 6);

-- 13. Messages
INSERT INTO messages (id, chat_id, content, sent_at, user_id, is_read) VALUES
(1, 1, 'Hello, I would like to ask about your availability for mathematics.', '2026-05-10 10:00:00', 3, TRUE),
(2, 1, 'Hi! Sure, you can find all my available time slots directly on my profile.', '2026-05-10 10:15:00', 2, TRUE),
(3, 2, 'Do you prepare students for advanced level physics exams?', '2026-05-11 14:20:00', 3, TRUE),
(4, 2, 'Yes, absolutely. Most of my current students are preparing for their finals.', '2026-05-11 14:35:00', 2, TRUE),
(5, 3, 'Thank you for today''s lesson, it helped me a lot.', '2026-05-12 16:00:00', 3, TRUE),
(6, 3, 'It was my absolute pleasure! See you next week.', '2026-05-12 16:10:00', 2, TRUE),
(7, 4, 'Do I need to purchase a specific textbook for our English classes?', '2026-05-12 17:00:00', 3, TRUE),
(8, 7, 'Yes, check your email.', '2026-05-13 18:05:00', 1, TRUE);

-- Aktualizacja sekwencji
SELECT setval(pg_get_serial_sequence('roles',              'id'), (SELECT MAX(id) FROM roles));
SELECT setval(pg_get_serial_sequence('statuses',           'id'), (SELECT MAX(id) FROM statuses));
SELECT setval(pg_get_serial_sequence('global_limits',      'id'), (SELECT MAX(id) FROM global_limits));
SELECT setval(pg_get_serial_sequence('availability_slots', 'id'), (SELECT MAX(id) FROM availability_slots));
SELECT setval(pg_get_serial_sequence('subjects',           'id'), (SELECT MAX(id) FROM subjects));
SELECT setval(pg_get_serial_sequence('users',              'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('offers',             'id'), (SELECT MAX(id) FROM offers));
SELECT setval(pg_get_serial_sequence('offer_slots',        'id'), (SELECT MAX(id) FROM offer_slots));
SELECT setval(pg_get_serial_sequence('bookings',           'id'), (SELECT MAX(id) FROM bookings));
SELECT setval(pg_get_serial_sequence('reviews',            'id'), (SELECT MAX(id) FROM reviews));
SELECT setval(pg_get_serial_sequence('chats',              'id'), (SELECT MAX(id) FROM chats));
SELECT setval(pg_get_serial_sequence('messages',           'id'), (SELECT MAX(id) FROM messages));

-- Hashowanie haseł
CREATE EXTENSION IF NOT EXISTS pgcrypto;
UPDATE users SET password = crypt(password, gen_salt('bf')) WHERE password NOT LIKE '$2a$%';