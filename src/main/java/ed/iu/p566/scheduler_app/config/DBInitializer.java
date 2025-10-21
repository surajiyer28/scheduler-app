package ed.iu.p566.scheduler_app.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
        prof.setPassword("password");  
        prof.setRole(UserRole.PROFESSOR);
        users.add(prof);
        
        User ta = new User();
        ta.setName("TA");
        ta.setEmail("ta@iu.edu");
        ta.setPassword("password");
        ta.setRole(UserRole.TA);
        users.add(ta);
        
        User student = new User();
        student.setName("Student");
        student.setEmail("student@iu.edu");
        student.setPassword("password");
        student.setRole(UserRole.STUDENT);
        users.add(student);
        
        userRepository.saveAll(users);


    }
    
    private void createSampleAppointments() {

        User prof = userRepository.findByEmail("prof@iu.edu").orElse(null);
        
        if (prof == null) {
            System.out.println("Weird, couldn't find the professor we just created");
            return;

        }
        

        AppointmentGroup group = new AppointmentGroup();
        group.setTitle("Sprint1");
        group.setType(AppointmentType.INDIVIDUAL); 
        group.setDurationPerSlot(30); 
        group.setProfessorId(prof.getId());
        group.setCreatedAt(LocalDateTime.now());
        

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfter = LocalDate.now().plusDays(2);
        
        List<AvailabilitySlot> availabilitySlots = new ArrayList<>();
        
        AvailabilitySlot morningSlot = new AvailabilitySlot(
            tomorrow, 
            LocalTime.of(9, 0),  
            LocalTime.of(11, 0)   
        );
        availabilitySlots.add(morningSlot);
        
        AvailabilitySlot afternoonSlot = new AvailabilitySlot(
            dayAfter, 
            LocalTime.of(14, 0), 
            LocalTime.of(16, 0)  
        );
        availabilitySlots.add(afternoonSlot);

        group.setAvailabilitySlots(availabilitySlots);
        

        AppointmentGroup savedGroup = appointmentGroupRepository.save(group);
        
        List<TimeSlot> timeSlots = TimeSlotUtility.generateTimeSlots(savedGroup);
        timeSlotRepository.saveAll(timeSlots);


        System.out.println("Created test appointment group and time slots");
        
    
    }
}
