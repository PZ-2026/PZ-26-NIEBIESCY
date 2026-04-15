package com.vectorpeaks.edulink.data

import com.vectorpeaks.edulink.data.model.*

object FakeData {

    // ==================== UŻYTKOWNICY ====================
    val users = listOf(
        User(
            id = 1,
            firstName = "Jan",
            lastName = "Kowalski",
            email = "admin@edulink.pl",
            password = "admin123",
            role = RoleID.ADMIN,
            phone = "500100200",
            address = "Rzeszów"
        ),
        User(
            id = 2,
            firstName = "Anna",
            lastName = "Nowak",
            email = "tutor@edulink.pl",
            password = "tutor123",
            role = RoleID.TUTOR,
            phone = "600200300",
            address = "Rzeszów"
        ),
        User(
            id = 3,
            firstName = "Piotr",
            lastName = "Wiśniewski",
            email = "student@edulink.pl",
            password = "student123",
            role = RoleID.STUDENT,
            phone = "700300400",
            address = "Rzeszów"
        ),
        User(
            id = 4,
            firstName = "Maria",
            lastName = "Zielińska",
            email = "tutor2@edulink.pl",
            password = "tutor123",
            role = RoleID.TUTOR,
            phone = "600400500",
            address = "Kraków"
        ),
        User(
            id = 5,
            firstName = "Tomasz",
            lastName = "Lewandowski",
            email = "student2@edulink.pl",
            password = "student123",
            role = RoleID.STUDENT,
            phone = "700500600",
            address = "Warszawa"
        )
    )

    fun authenticateUser(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }

    // ==================== OFERTY ====================
    val offers = listOf(
        Offer(
            id = 1,
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Matematyka",
            description = "Korepetycje z matematyki dla licealistów i studentów. Analiza matematyczna, algebra liniowa, rachunek prawdopodobieństwa.",
            pricePerHour = 80.0,
            city = "Rzeszów",
            isOnline = true,
            rating = 4.8f,
            reviewCount = 24,
            availableSlots = listOf("Pon 10:00", "Śr 14:00", "Pt 16:00")
        ),
        Offer(
            id = 2,
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Fizyka",
            description = "Fizyka dla maturzystów – mechanika, termodynamika, elektryczność. Przygotowanie do matury rozszerzonej.",
            pricePerHour = 90.0,
            city = "Rzeszów",
            isOnline = false,
            rating = 4.6f,
            reviewCount = 12,
            availableSlots = listOf("Wt 12:00", "Czw 15:00")
        ),
        Offer(
            id = 3,
            tutorId = 4,
            tutorName = "Maria Zielińska",
            subject = "Język angielski",
            description = "Konwersacje, gramatyka, przygotowanie do FCE/CAE. Zajęcia dostosowane do poziomu ucznia.",
            pricePerHour = 70.0,
            city = "Kraków",
            isOnline = true,
            rating = 4.9f,
            reviewCount = 38,
            availableSlots = listOf("Pon 8:00", "Śr 10:00", "Pt 12:00", "Sob 9:00")
        ),
        Offer(
            id = 4,
            tutorId = 4,
            tutorName = "Maria Zielińska",
            subject = "Język niemiecki",
            description = "Korepetycje z języka niemieckiego od podstaw do poziomu B2. Przygotowanie do Goethe-Zertifikat.",
            pricePerHour = 75.0,
            city = "Kraków",
            isOnline = true,
            rating = 4.7f,
            reviewCount = 15,
            availableSlots = listOf("Wt 14:00", "Czw 16:00")
        ),
        Offer(
            id = 5,
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Informatyka",
            description = "Programowanie w Pythonie i Javie. Algorytmy, struktury danych, przygotowanie do olimpiad informatycznych.",
            pricePerHour = 100.0,
            city = "Rzeszów",
            isOnline = true,
            rating = 5.0f,
            reviewCount = 8,
            availableSlots = listOf("Pon 16:00", "Śr 18:00")
        ),
        Offer(
            id = 6,
            tutorId = 4,
            tutorName = "Maria Zielińska",
            subject = "Chemia",
            description = "Chemia organiczna i nieorganiczna. Pomoc w nauce dla uczniów szkół średnich.",
            pricePerHour = 85.0,
            city = "Kraków",
            isOnline = false,
            rating = 4.5f,
            reviewCount = 19,
            availableSlots = listOf("Pon 14:00", "Pt 10:00")
        )
    )

    // ==================== REZERWACJE ====================
    val reservations = listOf(
        Reservation(
            id = 1,
            offerId = 1,
            studentId = 3,
            studentName = "Piotr Wiśniewski",
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Matematyka",
            date = "2026-03-25",
            time = "10:00",
            price = 80.0,
            status = ReservationStatus.ACCEPTED
        ),
        Reservation(
            id = 2,
            offerId = 3,
            studentId = 3,
            studentName = "Piotr Wiśniewski",
            tutorId = 4,
            tutorName = "Maria Zielińska",
            subject = "Język angielski",
            date = "2026-03-26",
            time = "10:00",
            price = 70.0,
            status = ReservationStatus.PENDING
        ),
        Reservation(
            id = 3,
            offerId = 1,
            studentId = 5,
            studentName = "Tomasz Lewandowski",
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Matematyka",
            date = "2026-03-20",
            time = "14:00",
            price = 80.0,
            status = ReservationStatus.COMPLETED,
            rating = 5
        ),
        Reservation(
            id = 4,
            offerId = 2,
            studentId = 3,
            studentName = "Piotr Wiśniewski",
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Fizyka",
            date = "2026-03-18",
            time = "12:00",
            price = 90.0,
            status = ReservationStatus.REJECTED
        ),
        Reservation(
            id = 5,
            offerId = 5,
            studentId = 3,
            studentName = "Piotr Wiśniewski",
            tutorId = 2,
            tutorName = "Anna Nowak",
            subject = "Informatyka",
            date = "2026-03-15",
            time = "16:00",
            price = 100.0,
            status = ReservationStatus.COMPLETED,
            rating = 4
        )
    )

    // ==================== CZATY ====================
    val conversations = listOf(
        ChatConversation(
            id = 1,
            otherUserId = 2,
            otherUserName = "Anna Nowak",
            lastMessage = "Dziękuję za lekcję!",
            lastMessageTime = "14:30",
            unreadCount = 0
        ),
        ChatConversation(
            id = 2,
            otherUserId = 4,
            otherUserName = "Maria Zielińska",
            lastMessage = "Czy możemy przesunąć zajęcia na piątek?",
            lastMessageTime = "wczoraj",
            unreadCount = 1
        )
    )

    val messages = listOf(
        Message(1, 1, 3, "Dzień dobry, chciałbym zapytać o zajęcia z matematyki.", "10:00"),
        Message(2, 1, 2, "Dzień dobry! Oczywiście, jakie tematy Pana interesują?", "10:05"),
        Message(3, 1, 3, "Analiza matematyczna – całki i pochodne.", "10:10"),
        Message(4, 1, 2, "Świetnie, mogę pomóc. Proponuję spotkanie w poniedziałek o 10:00.", "10:15"),
        Message(5, 1, 3, "Dziękuję za lekcję!", "14:30"),
        Message(6, 2, 3, "Dzień dobry, mam pytanie o zajęcia z angielskiego.", "Pon 9:00"),
        Message(7, 2, 4, "Dzień dobry! Słucham.", "Pon 9:30"),
        Message(8, 2, 3, "Czy możemy przesunąć zajęcia na piątek?", "wczoraj")
    )

    // ==================== PRZEDMIOTY ====================
    val subjects = listOf(
        "Matematyka", "Fizyka", "Chemia", "Biologia",
        "Język angielski", "Język niemiecki", "Język francuski",
        "Informatyka", "Historia", "Geografia", "Polski"
    )

    val cities = listOf("Rzeszów", "Kraków", "Warszawa", "Wrocław", "Gdańsk", "Poznań")
}
