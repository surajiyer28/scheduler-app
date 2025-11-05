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
1. Create appointment groups with titles
2. Set individual or group type
3. Add availability windows (dates/times)
4. System generates time slots automatically

**Students:**
1. Browse available appointments
2. Click a green slot to book
3. For groups, select classmates
4. One booking per appointment group
