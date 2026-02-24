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
             	return "registration";
      }
    /** {@inheritDoc} */
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public final String registration(final @ModelAttribute("userForm") User userForm, 
    	final BindingResult bindingResult, final Model model) {
    	
        userValidator.validate(userForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "registration";
        }
	    LOGGER.info("User PWD: {}", userForm.getPassword());
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
        return "login";
    }
    /** {@inheritDoc} */
    @RequestMapping(value = { "/", "/welcome"}, method = RequestMethod.GET)
    public final String welcome(final Model model) {
        return "welcome";
    }
    /** {@inheritDoc} */
    @RequestMapping(value = { "/index"} , method = RequestMethod.GET)
    public final String indexHome(final Model model) {
        return "index_home";
    }
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String getAllUsers(Model model)
    {	
   
        List<User> users = userService.getList();
	    LOGGER.info("All User Data:::{}", users);
        model.addAttribute("users", users);
        return "userList";
    }
    
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
    public String getOneUser(@PathVariable(value="id") String id,Model model)
    {	
        String result ="";
    	try{
    		if( id != null && MemcachedUtils.memcachedGetData(id)!= null){    			
    			User userData =  MemcachedUtils.memcachedGetData(id);
        		result ="Data is From Cache";
        		LOGGER.info(SEPARATOR);
        		LOGGER.info("Data is From Cache !!");
        		LOGGER.info(SEPARATOR);
        		LOGGER.info("Father ::: {}", userData.getFatherName());
    			model.addAttribute("user", userData);
        		model.addAttribute("Result", result);
    		}
    		else{
	    		User user = userService.findById(Long.parseLong(id)); 
        		result = MemcachedUtils.memcachedSetData(user,id);
        		if(result == null ){
        			result ="Memcached Connection Failure !!";
	    		}
        		LOGGER.info(SEPARATOR);
        		LOGGER.info("Data is From Database");
        		LOGGER.info(SEPARATOR);
        		LOGGER.info("Result ::: {}", result);	       
		        model.addAttribute("user", user);
        		model.addAttribute("Result", result);
    		}
    	} catch (Exception e) {    		
        	LOGGER.error("Failed to load user {}", id, e);
		}
        return "user";
    }
    
    /** {@inheritDoc} */
    @RequestMapping(value = { "/user/{username}"} , method = RequestMethod.GET)
    public final String userUpdate(@PathVariable(value="username") String username,final Model model) {
    	User user = userService.findByUsername(username); 
        LOGGER.info("User Data:::{}", user);
    	model.addAttribute("user", user);
    	return "userUpdate";
    }
    @RequestMapping(value = { "/user/{username}"} , method = RequestMethod.POST)
    public final String userUpdateProfile(@PathVariable(value="username") String username,final @ModelAttribute("user") User userForm,final Model model) {
    	User user = userService.findByUsername(username);
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
    	userService.save(user); 
    	return "welcome";
    }
    
    @RequestMapping(value={"/user/rabbit"}, method={RequestMethod.GET})
    public String rabbitmqSetUp() { 
	    LOGGER.info("Rabbit mq method is callled!!!");
      for (int i = 0; i < 20; i++) {
        producerService.produceMessage(generateString());
      }
      return "rabbitmq";
    }
    
    private static String generateString() {
      String uuid = UUID.randomUUID().toString();
      return "uuid = " + uuid;
    }
    

    
}
