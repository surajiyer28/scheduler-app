package ed.iu.p566.scheduler_app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import org.springframework.ui.Model;
import ed.iu.p566.scheduler_app.model.User;
import ed.iu.p566.scheduler_app.model.User.UserRole;
import ed.iu.p566.scheduler_app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AppControllerTest {

    @InjectMocks
    private AppController appController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Model model;

    @Mock
    private SessionStatus sessionStatus;

    private RedirectAttributes redirectAttributes;

    private User testUser;

    @BeforeEach
    void testSetup(){
        redirectAttributes = new RedirectAttributesModelMap();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("testuser@iu.edu");
        testUser.setPassword("password");
        testUser.setRole(UserRole.STUDENT);

    }

    @Test
    void testCreateUser_Success(){
        when(userRepository.findByEmail("newuser@iu.edu")).thenReturn(Optional.empty());

        User newUser = new User("New User","newuser@iu.edu","password",UserRole.STUDENT);
        String result = appController.createUser(newUser, redirectAttributes);

        assertEquals("redirect:/", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("message"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        assertEquals("newuser@iu.edu",userCaptor.getValue().getEmail());
    }

    @Test
    void testCreateUser_InvalidEmailFailure(){
        // when(userRepository.findByEmail("newuser@iu.edu")).thenReturn(Optional.empty());

        User newUser = new User("New User","newuser@gmail.com","password",UserRole.STUDENT);
        String result = appController.createUser(newUser, redirectAttributes);

        assertEquals("redirect:/signup", result);
        assertEquals("Email must be an IU email.",redirectAttributes.getFlashAttributes().get("error"));

        verify(userRepository, never()).save(any());        
    }


    @Test
    void testCreateUser_UserExistsFailure(){
        when(userRepository.findByEmail("testuser@iu.edu")).thenReturn(Optional.of(testUser));

        User newUser = new User("New User","testuser@iu.edu","password",UserRole.STUDENT);
        String result = appController.createUser(newUser, redirectAttributes);

        assertEquals("redirect:/signup", result);
        assertEquals("This email already has an account associated with it.",redirectAttributes.getFlashAttributes().get("error"));

        verify(userRepository, never()).save(any());        
    }
    

    @Test
    void testLogin_Success(){
        testUser.setPassword("password");
        when(userRepository.findByEmail("testuser@iu.edu")).thenReturn(Optional.of(testUser));

        String result = appController.loginUser("testuser@iu.edu", "password", redirectAttributes, model);

        assertEquals("redirect:/dashboard", result);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("message"));
        verify(model).addAttribute("currentUser", testUser);      
    }


    @Test
    void testLogin_IncorrectPasswordFailure(){
        testUser.setPassword("correctpassword");
        when(userRepository.findByEmail("testuser@iu.edu")).thenReturn(Optional.of(testUser));

        String result = appController.loginUser("testuser@iu.edu", "password", redirectAttributes, model);

        assertEquals("redirect:/", result);
        assertEquals("Incorrect password. Please try again.",redirectAttributes.getFlashAttributes().get("error"));       
    }

    @Test
    void testLogin_NotFoundFailure(){
        when(userRepository.findByEmail("notestuser@iu.edu")).thenReturn(Optional.empty());

        String result = appController.loginUser("notestuser@iu.edu", "password", redirectAttributes, model);

        assertEquals("redirect:/", result);
        assertEquals("We couldn't find an account with these credentials.",redirectAttributes.getFlashAttributes().get("error"));

    }


    @Test
    void testLogout() {
        String result = appController.logout(sessionStatus);

        assertEquals("redirect:/", result);
        verify(sessionStatus).setComplete();
    }
}
