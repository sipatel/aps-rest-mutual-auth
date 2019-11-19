/**
 * Copyright 2005-2018 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.activiti.test.services;

import static com.activiti.security.SecurityUtils.getCurrentLogin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.test.ApplicationTestConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.activiti.domain.User;
import com.activiti.domain.representation.BpmSuiteMasterConfigurationRepresentation;
import com.activiti.domain.representation.MasterConfigurationRepresentation;
import com.activiti.repository.ClusterConfigRepository;
import com.activiti.repository.MasterConfigRepository;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.repository.UserRepository;
import com.activiti.service.UserService;
import com.activiti.service.activiti.ClusterConfigService;
import com.activiti.service.activiti.MasterConfigurationService;
import com.activiti.service.activiti.ServerConfigService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.dto.ClusterConfigRepresentation;
import com.activiti.web.rest.dto.ServerConfigRepresentation;

/**
 * A test that does some CRUD operations to verify database is allright.
 * Needs more love, obviously.
 *
 * @author Joram Barrez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ApplicationTestConfiguration.class)
public class CrudTest {
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClusterConfigService clusterConfigService;
    
    @Autowired
    private ClusterConfigRepository clusterConfigRepository;
    
    @Autowired
    private ServerConfigService serverConfigService;
    
    @Autowired
    private ServerConfigRepository serverConfigRepository;
    
    @Autowired
    private MasterConfigurationService masterConfigurationService;
    
    @Autowired
    private MasterConfigRepository masterConfigRepository;
    
    @Before
    public void setup() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User
            ("testUser", "testUser", new ArrayList<GrantedAuthority>(grantedAuthorities));
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    @After
    public void cleanup() {
        masterConfigRepository.deleteAll();
        serverConfigRepository.deleteAll();
        clusterConfigRepository.deleteAll();
        userRepository.deleteAll();
        
        SecurityContextHolder.clearContext();
    }
    
    @Test
    public void testCreateUser() {
        userService.createAdminUser("jos", "jos", "Jos", "Jossen", "jos@alfresco.com");
        
        List<User> users = userRepository.findAll();
        int count = users.size();
        for (User user : users) {
            if (user.getLogin().equals("jos")) {
                Assert.assertEquals("Jos", user.getFirstName());
                Assert.assertEquals("Jossen", user.getLastName());
                Assert.assertEquals("jos@alfresco.com", user.getEmail());
            }
        }
        
        userService.deleteUser("jos");
        Assert.assertEquals(count - 1, userRepository.findAll().size());
    }
    
    @Test
    public void testCreateClusterConfig() {
        ClusterConfigRepresentation clusterConfig = clusterConfigService.createNewClusterConfig("testCluster", "testClusterUser", "password", true);
        
        Assert.assertEquals(clusterConfig.getClusterName(), clusterConfigService.findClusterConfigByName("testCluster").getClusterName());
        Assert.assertEquals("testClusterUser", clusterConfigService.findClusterConfigByName("testCluster").getUser().getLogin());
    }
    
    @Test
    public void testCreateServerConfig() {
        
        long count = serverConfigRepository.count();
        ClusterConfigRepresentation clusterConfig = clusterConfigService.createNewClusterConfig("testCluster", "testClusterUser", "password", true);
        Assert.assertEquals(count + 1, serverConfigRepository.count());
        
        // Server config gets created when creating a new clusterconfig
        List<ServerConfigRepresentation> serverConfigs = serverConfigService.findByClusterConfigId(clusterConfig.getId());
        Assert.assertEquals(1, serverConfigs.size());
        
        ServerConfigRepresentation serverConfig = serverConfigs.get(0);
        Assert.assertEquals("http://localhost", serverConfig.getServerAddress());
        Assert.assertEquals(9999L, serverConfig.getServerPort().longValue());
        Assert.assertEquals("Process services", serverConfig.getName());
        Assert.assertEquals("activiti-app", serverConfig.getContextRoot());
        Assert.assertEquals("admin@app.activiti.com", serverConfig.getUserName());
        Assert.assertEquals("api", serverConfig.getRestRoot());
        Assert.assertNotEquals("admin", serverConfig.getPassword()); // Password should be encrypted!
        
    }
    
    @Test
    public void testCreateMasterConfiguration() {
        
        ClusterConfigRepresentation clusterConfig = clusterConfigService.createNewClusterConfig("testCluster", "testClusterUser", "password", true);
        
        long count = masterConfigRepository.count();
        masterConfigurationService.storeBpmSuiteMasterConfiguration(clusterConfig.getId(), "this is a test");
        Assert.assertEquals(count + 1, masterConfigRepository.count());
        
        MasterConfigurationRepresentation masterConfigurationRepresentation = masterConfigurationService.findByClusterConfigId(clusterConfig.getId());
        Assert.assertTrue(masterConfigurationRepresentation instanceof BpmSuiteMasterConfigurationRepresentation);
        
        BpmSuiteMasterConfigurationRepresentation bpmSuiteMasterConfigurationRepresentation = (BpmSuiteMasterConfigurationRepresentation) masterConfigurationRepresentation;
        Assert.assertEquals("bpmSuite", bpmSuiteMasterConfigurationRepresentation.getType());
        Assert.assertEquals("this is a test", bpmSuiteMasterConfigurationRepresentation.getPropertiesText());
        
        String uuid = bpmSuiteMasterConfigurationRepresentation.getConfigId();
        Assert.assertNotNull(uuid);
        
        // Updating the master config should change the uid
        masterConfigurationService.storeBpmSuiteMasterConfiguration(clusterConfig.getId(), "this is another test");
        masterConfigurationRepresentation = masterConfigurationService.findByClusterConfigId(clusterConfig.getId());
        bpmSuiteMasterConfigurationRepresentation = (BpmSuiteMasterConfigurationRepresentation) masterConfigurationRepresentation;
        Assert.assertEquals("bpmSuite", bpmSuiteMasterConfigurationRepresentation.getType());
        Assert.assertEquals("this is another test", bpmSuiteMasterConfigurationRepresentation.getPropertiesText());
        
        Assert.assertNotEquals(uuid, bpmSuiteMasterConfigurationRepresentation.getConfigId());
    }
    
    @Test(expected = ActivitiServiceException.class)
    public void updateUser_ReadOnlyUserAndUpdatedUserIsAuthenticated_ExceptionThrown() {
        String currentLogin = getCurrentLogin();
        Boolean isReadOnlyUser = Boolean.TRUE;
        userService.updateUser(currentLogin, null, null, null, isReadOnlyUser);
    }
}
