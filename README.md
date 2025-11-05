# Appointment Scheduler
### A simple web app where professors can post their availability, and students can book time slots to meet with their professors. Additionally, TAs can view these bookings.

## Tech Stack:
* Java 17
* Spring Boot
* SQLite
* Bootstrap
* Docker

Live at: https://appointment-scheduler-z31a.onrender.com/

## Quick Start:
```bash
# Clone and run
git clone 
cd scheduler-app
./mvnw spring-boot:run
```

## How It Works

**Professors:**
1. Create appointment groups with titles, durations and their availability.
2. System generates time slots automatically with the given duration and an optional gap.
3. Professors can also modify existing appointment groups or bookings.

**Students:**
1. Can browse through different appointment groups and book time slots within appointment groups (one timeslot per group).
2. Students can also modify or cancel an existing booking.

**TAs:**
1. Browse through all available time slots within different appointment groups.
2. View details of bookings such as date, time and who the slot has been booked by.
