package com.visualpathit.account.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.visualpathit.account.model.User;
import com.visualpathit.account.service.UserService;
import com.visualpathit.account.utils.ElasticsearchUtil;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Controller
public class ElasticSearchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchController.class);
    private static final String INDEX_USERS = "users";
    private static final String TYPE_USER = "user";
    private static final String INDEX_EMPLOYEE = "employee";
    private static final String TYPE_ID = "id";
    private static final String VIEW_ELASTICSEARCH_RESULT = "elasticeSearchRes";
    private static final String MODEL_RESULT_ATTR = "res";
    private static final String RESULT_USERS = "Users";

	@Autowired
    private UserService userService;

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;
    
    @RequestMapping(value="/user/elasticsearch", method=RequestMethod.GET)
    public String insert(final Model model) throws IOException {
    	List<User> users = userService.getList();
    	for (User user : users) {
    	    	IndexResponse response = elasticsearchUtil.transportClient().prepareIndex(INDEX_USERS, TYPE_USER, String.valueOf(user.getId()))
                .setSource(jsonBuilder()
                        .startObject()
                        .field("name", user.getUsername())
                        .field("DOB",user.getDateOfBirth())
                        .field("fatherName",user.getFatherName())
                        .field("motherName",user.getMotherName())
                        .field("gender",user.getGender())
                        .field("nationality",user.getNationality())
                        .field("phoneNumber", user.getPhoneNumber())
                        .endObject()
                )
                .get();
        LOGGER.info("{}", response.getResult());
    	}
        model.addAttribute(MODEL_RESULT_ATTR, RESULT_USERS);
        return VIEW_ELASTICSEARCH_RESULT;
        		
    }

    @RequestMapping(value="/rest/users/view/{id}", method=RequestMethod.GET)
    public String  view(@PathVariable final String id,final Model model) {
	    GetResponse getResponse = elasticsearchUtil.transportClient().prepareGet(INDEX_USERS, TYPE_USER, id).get();
	    LOGGER.info("{}", getResponse.getSource());
        
        model.addAttribute(MODEL_RESULT_ATTR, getResponse.getSource().get("name"));
       
        return VIEW_ELASTICSEARCH_RESULT;
    }
    @RequestMapping(value="/rest/users/update/{id}", method=RequestMethod.GET)
    public String update(@PathVariable final String id,final Model model) throws IOException {

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(INDEX_EMPLOYEE)
                .type(TYPE_ID)
                .id(id)
                .doc(jsonBuilder()
                        .startObject()
                        .field("gender", "male")
                        .endObject());
        try {
            UpdateResponse updateResponse = elasticsearchUtil.transportClient().update(updateRequest).get();
	        LOGGER.info("{}", updateResponse.status());
            model.addAttribute(MODEL_RESULT_ATTR, updateResponse.status());
            return VIEW_ELASTICSEARCH_RESULT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
	        LOGGER.warn("Elasticsearch update interrupted", e);
        } catch (ExecutionException e) {
	        LOGGER.error("Elasticsearch update failed", e);
        }
	    return VIEW_ELASTICSEARCH_RESULT;
    }
    @RequestMapping(value="/rest/users/delete/{id}", method=RequestMethod.GET)
    public String delete(@PathVariable final String id,final Model model) {

	    DeleteResponse deleteResponse =elasticsearchUtil.transportClient().prepareDelete(INDEX_EMPLOYEE, TYPE_ID, id).get();
	    LOGGER.info("{}", deleteResponse.getResult());
	    model.addAttribute(MODEL_RESULT_ATTR, deleteResponse.getResult().toString());
	    return VIEW_ELASTICSEARCH_RESULT;
    }
}
