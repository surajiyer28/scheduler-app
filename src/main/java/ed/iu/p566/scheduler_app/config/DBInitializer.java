package ed.iu.p566.scheduler_app.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ed.iu.p566.scheduler_app.model.AppointmentGroup;
import ed.iu.p566.scheduler_app.model.AppointmentGroup.AppointmentType;
import ed.iu.p566.scheduler_app.model.AvailabilitySlot;
import ed.iu.p566.scheduler_app.model.TimeSlot;
import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.User.UserRole;
import ed.iu.p566.scheduler_app.repository.AppointmentGroupRepository;
import ed.iu.p566.scheduler_app.repository.TimeSlotRepository;
import ed.iu.p566.scheduler_app.repository.UserRepository;
import ed.iu.p566.scheduler_app.utilities.TimeSlotUtility;


@Component
public class DBInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentGroupRepository appointmentGroupRepository;
    
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    
    @Override
    public void run(String... args) throws Exception {
        
        if (userRepository.count() == 0) {
            System.out.println("No users found - adding some sample data to get started");
            
            // adding some test users and appointments
            createSampleUsers();
            createSampleAppointments();
            
            System.out.println("Sample data added");
        } else {
            
            System.out.println("No Sample data added");
        }
    }
    
    private void createSampleUsers() {
        
        List<User> users = new ArrayList<>();
        
        
        User prof = new User();
        prof.setName("Professor");
        prof.setEmail("prof@iu.edu");
        prof.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        prof.setRole(UserRole.PROFESSOR);
        users.add(prof);
        
        User ta = new User();
        ta.setName("TA");
        ta.setEmail("ta@iu.edu");
        ta.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
        ta.setRole(UserRole.TA);
        users.add(ta);
        
        for (int i = 1; i <= 5; i++) {
            User student = new User();
            student.setName("Student" + i);
            student.setEmail("student" + i + "@iu.edu");
            student.setPassword(BCrypt.hashpw("password", BCrypt.gensalt()));
            student.setRole(UserRole.STUDENT);
            users.add(student);
        }

        
        userRepository.saveAll(users);


    }
    
    private void createSampleAppointments() {

        User prof = userRepository.findByEmail("prof@iu.edu").orElse(null);
        
        if (prof == null) {
            System.out.println("Couldn't find the professor we just created");
            return;

        }

        AppointmentGroup group = new AppointmentGroup();
        group.setTitle("Sprint1");
        group.setType(AppointmentType.INDIVIDUAL); 
        group.setDurationPerSlot(30); 
        group.setGapBetweenSlots(0);
        group.setProfessorId(prof.getId());
        group.setCreatedAt(LocalDateTime.now());
        
        
        List<AvailabilitySlot> availabilitySlots = new ArrayList<>();
        
        AvailabilitySlot morningSlot = new AvailabilitySlot(
            LocalDate.now().plusDays(1), 
            LocalTime.of(9, 0),  
            LocalTime.of(11, 0)   
        );
        availabilitySlots.add(morningSlot);
        
        AvailabilitySlot afternoonSlot = new AvailabilitySlot(
            LocalDate.now().plusDays(2), 
            LocalTime.of(14, 0), 
            LocalTime.of(16, 0)  
        );
        availabilitySlots.add(afternoonSlot);

        group.setAvailabilitySlots(availabilitySlots);
        

        AppointmentGroup savedGroup = appointmentGroupRepository.save(group);
        
        List<TimeSlot> timeSlots = TimeSlotUtility.generateTimeSlots(savedGroup);
        timeSlotRepository.saveAll(timeSlots);

        AppointmentGroup pastGroup = new AppointmentGroup();
        pastGroup.setTitle("Past Test Slot");
        pastGroup.setType(AppointmentType.GROUP); 
        pastGroup.setDurationPerSlot(45); 
        pastGroup.setGapBetweenSlots(5);
        pastGroup.setProfessorId(prof.getId());
        pastGroup.setCreatedAt(LocalDateTime.now().minusDays(5)); // Created 5 days ago
        
        
        List<AvailabilitySlot> pastAvailabilitySlots = new ArrayList<>();
        
        AvailabilitySlot pastSlot1 = new AvailabilitySlot(
            LocalDate.now().minusDays(2), 
            LocalTime.of(10, 0),  
            LocalTime.of(12, 0)   
        );
        pastAvailabilitySlots.add(pastSlot1);
        
        AvailabilitySlot pastSlot2 = new AvailabilitySlot(
            LocalDate.now().minusDays(1), 
            LocalTime.of(13, 0), 
            LocalTime.of(15, 0)  
        );
        pastAvailabilitySlots.add(pastSlot2);

        AvailabilitySlot futureSlot = new AvailabilitySlot(
            LocalDate.now().plusDays(1),
            LocalTime.of(13, 0), 
            LocalTime.of(15, 0)  
        );
        pastAvailabilitySlots.add(futureSlot);

        pastGroup.setAvailabilitySlots(pastAvailabilitySlots);
        
        AppointmentGroup savedPastGroup = appointmentGroupRepository.save(pastGroup);
        
        List<TimeSlot> pastTimeSlots = TimeSlotUtility.generateTimeSlots(savedPastGroup);
        timeSlotRepository.saveAll(pastTimeSlots);

        System.out.println("Created test appointment groups and time slots");
        
    
    }
}
