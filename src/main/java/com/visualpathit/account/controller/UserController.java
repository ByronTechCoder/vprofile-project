package com.visualpathit.account.controller;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.ProducerService;
import com.visualpathit.account.service.SecurityService;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.MemcachedUtils;
import com.visualpathit.account.validator.UserValidator;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
/**{@author imrant}*/
@Controller
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final String SEPARATOR = "--------------------------------------------";
    private static final String MODEL_USER_ATTR = "user";
    private static final String MODEL_RESULT_ATTR = "Result";
    private static final String VIEW_REGISTRATION = "registration";
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_WELCOME = "welcome";
    private static final String VIEW_INDEX_HOME = "index_home";
    private static final String VIEW_USER_LIST = "userList";
    private static final String VIEW_USER = "user";
    private static final String VIEW_USER_UPDATE = "userUpdate";
    private static final String VIEW_RABBIT = "rabbitmq";
    private static final String CACHE_HIT_MESSAGE = "Data is From Cache";
    private static final String CACHE_HIT_LOG = "Data is From Cache !!";
    private static final String DB_HIT_LOG = "Data is From Database";
    private static final String MEMCACHED_FAILURE_MESSAGE = "Memcached Connection Failure !!";

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;
    
    @Autowired
    private ProducerService producerService;
    
    /** {@inheritDoc} */
    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public final String registration(final Model model) {
        model.addAttribute("userForm", new User());
	    	return VIEW_REGISTRATION;
      }
    /** {@inheritDoc} */
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public final String registration(final @ModelAttribute("userForm") User userForm, 
    	final BindingResult bindingResult, final Model model) {
    	
        userValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
	        return VIEW_REGISTRATION;
        }
	    LOGGER.info("Registering user {}", userForm.getUsername());
        userService.save(userForm);

        securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

        return "redirect:/welcome";
    }
    /** {@inheritDoc} */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public final String login(final Model model, final String error, final String logout) {
	    LOGGER.info("Model data {}", model);
    	if (error != null){
            model.addAttribute("error", "Your username and password is invalid.");
        }
        if (logout != null){
            model.addAttribute("message", "You have been logged out successfully.");
        }
	    return VIEW_LOGIN;
    }
    /** {@inheritDoc} */
    @RequestMapping(value = { "/", "/welcome"}, method = RequestMethod.GET)
    public final String welcome(final Model model) {
	    return VIEW_WELCOME;
    }
    /** {@inheritDoc} */
    @RequestMapping(value = { "/index"} , method = RequestMethod.GET)
    public final String indexHome(final Model model) {
	    return VIEW_INDEX_HOME;
    }
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getAllUsers(Model model)
    {	
   
        List<User> users = userService.getList();
	    LOGGER.info("All User Data:::{}", users);
        model.addAttribute("users", users);
	    return VIEW_USER_LIST;
    }
    
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public String getOneUser(@PathVariable(value="id") String id,Model model)
    {	
        if (id == null) {
        	LOGGER.warn("User id is missing");
	        return VIEW_USER;
        }

        String result = "";
        User cachedUser = MemcachedUtils.memcachedGetData(id);
        if (cachedUser != null) {
        	result = CACHE_HIT_MESSAGE;
        	logDataSource(CACHE_HIT_LOG, cachedUser.getFatherName(), result);
        	model.addAttribute(MODEL_USER_ATTR, cachedUser);
        	model.addAttribute(MODEL_RESULT_ATTR, result);
            return VIEW_USER;
        }

        try {
        	User user = userService.findById(Long.parseLong(id));
        	result = MemcachedUtils.memcachedSetData(user, id);
        	if (result == null) {
        		result = MEMCACHED_FAILURE_MESSAGE;
        	}
        	logDataSource(DB_HIT_LOG, user.getFatherName(), result);
        	model.addAttribute(MODEL_USER_ATTR, user);
        	model.addAttribute(MODEL_RESULT_ATTR, result);
        } catch (NumberFormatException e) {
        	LOGGER.warn("Invalid user id {}", id, e);
        }
	    return VIEW_USER;
    }
    
    /** {@inheritDoc} */
    @RequestMapping(value = { "/user/{username}"} , method = RequestMethod.GET)
    public final String userUpdate(@PathVariable(value="username") String username,final Model model) {
    	User user = userService.findByUsername(username); 
        LOGGER.info("User Data:::{}", user);
        model.addAttribute(MODEL_USER_ATTR, user);
        return VIEW_USER_UPDATE;
    }
    @RequestMapping(value = { "/user/{username}"} , method = RequestMethod.POST)
    public final String userUpdateProfile(@PathVariable(value="username") String username,final @ModelAttribute("user") User userForm,final Model model) {
    	User user = userService.findByUsername(username);
        applyUserForm(user, userForm);
    	userService.save(user); 
        return VIEW_WELCOME;
    }
    
    @RequestMapping(value={"/user/rabbit"}, method={RequestMethod.GET})
    public String rabbitmqSetUp() { 
	    LOGGER.info("Rabbit mq method is callled!!!");
      for (int i = 0; i < 20; i++) {
        producerService.produceMessage(generateString());
      }
	    return VIEW_RABBIT;
    }
    
    private static String generateString() {
        return "uuid = " + UUID.randomUUID();
    }

    private void applyUserForm(User user, User userForm) {
        user.setUsername(userForm.getUsername());
        user.setUserEmail(userForm.getUserEmail());
        user.setDateOfBirth(userForm.getDateOfBirth());
        user.setFatherName(userForm.getFatherName());
        user.setMotherName(userForm.getMotherName());
        user.setGender(userForm.getGender());
        user.setLanguage(userForm.getLanguage());
        user.setMaritalStatus(userForm.getMaritalStatus());
        user.setNationality(userForm.getNationality());
        user.setPermanentAddress(userForm.getPermanentAddress());
        user.setTempAddress(userForm.getTempAddress());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setSecondaryPhoneNumber(userForm.getSecondaryPhoneNumber());
        user.setPrimaryOccupation(userForm.getPrimaryOccupation());
        user.setSecondaryOccupation(userForm.getSecondaryOccupation());
        user.setSkills(userForm.getSkills());
        user.setWorkingExperience(userForm.getWorkingExperience());
    }

    private void logDataSource(String dataSource, String fatherName, String result) {
        LOGGER.info(SEPARATOR);
        LOGGER.info(dataSource);
        LOGGER.info(SEPARATOR);
        LOGGER.info("Father ::: {}", fatherName);
        LOGGER.info("Result ::: {}", result);
    }
    

    
}
