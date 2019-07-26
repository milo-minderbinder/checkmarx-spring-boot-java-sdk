package com.checkmarx.sdk.service;

import checkmarx.wsdl.portal.*;
import com.checkmarx.sdk.config.CxProperties;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.exception.CheckmarxLegacyException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.beans.ConstructorProperties;
import java.util.List;

/**
 * Checkmarx SOAP WebService Client
 */
@Component
public class CxLegacyService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CxLegacyService.class);
    private final CxProperties properties;
    private final WebServiceTemplate ws;

    private static final String CX_WS_PREFIX= "http://Checkmarx.com/";
    private static final String CX_WS_LOGIN_URI = CX_WS_PREFIX + "LoginV2";
    private static final String CX_WS_DESCRIPTION_URI = CX_WS_PREFIX + "GetResultDescription";
    private static final String CX_WS_LDAP_CONFIGURATIONS_URI = CX_WS_PREFIX + "GetLdapServersConfigurations";
    private static final String CX_WS_TEAM_LDAP_MAPPINGS_URI = CX_WS_PREFIX + "GetTeamLdapGroupsMapping";
    private static final String CX_WS_UPDATE_TEAM_URI = CX_WS_PREFIX + "UpdateTeam";
    private static final String CX_WS_TEAM_URI = CX_WS_PREFIX + "CreateNewTeam";
    private static final String CX_WS_DELETE_TEAM_URI = CX_WS_PREFIX + "DeleteTeam";

    @ConstructorProperties({"properties", "ws"})
    public CxLegacyService(CxProperties properties, WebServiceTemplate ws) {
        this.properties = properties;
        this.ws = ws;
    }

    /**
     * Login to Cx using legacy SOAP WS
     * @param username
     * @param password
     * @return
     * @throws CheckmarxLegacyException
     */
    public String login(String username, String password) throws CheckmarxLegacyException {
        LoginV2 request = new LoginV2();
        request.setApplicationCredentials(new Credentials(username, password));
        LoginV2Response response = (LoginV2Response) ws.marshalSendAndReceive(ws.getDefaultUri(), request, new SoapActionCallback(CX_WS_LOGIN_URI));
        try {
            if(!response.getLoginV2Result().isIsSuccesfull())
                throw new CheckmarxLegacyException("Authentication Error");
            return response.getLoginV2Result().getSessionId();
        }
        catch(NullPointerException e){
            log.error("Authentication Error while logging into CX using SOAP WS");
            throw new CheckmarxLegacyException("Authentication Error");
        }
    }

    void createTeam(String sessionId, String parentId, String teamName) throws CheckmarxException {
        CreateNewTeam request = new CreateNewTeam(sessionId);
        request.setNewTeamName(teamName);
        request.setParentTeamID(parentId);
        log.info("Creating team {} ({})", teamName, parentId);

        try {
            CreateNewTeamResponse response = (CreateNewTeamResponse) ws.marshalSendAndReceive(ws.getDefaultUri(), request, new SoapActionCallback(CX_WS_TEAM_URI));
            if(!response.getCreateNewTeamResult().isIsSuccesfull()){
                log.error("Error occurred while creating Team {} with parentId {}", teamName, parentId);
                throw new CheckmarxException("Error occurred during team creation");
            }
        }catch(NullPointerException e){
            log.error("Error occurred while creating Team {} with parentId {}", teamName, parentId);
            throw new CheckmarxException("Error occurred during team creation");
        }
    }

    void deleteTeam(String sessionId, String teamId) throws CheckmarxException {
        DeleteTeam request = new DeleteTeam();
        request.setSessionID(sessionId);
        request.setTeamID(teamId);
        log.info("Deleting team id {}", teamId);

        try {
            DeleteTeamResponse response = (DeleteTeamResponse) ws.marshalSendAndReceive(ws.getDefaultUri(), request, new SoapActionCallback(CX_WS_DELETE_TEAM_URI));
            if(!response.getDeleteTeamResult().isIsSuccesfull()){
                log.error("Error occurred while deleting Team id {}", teamId);
                throw new CheckmarxException("Error occurred during team deletion");
            }
        }catch(NullPointerException e){
            log.error("Error occurred while deleting Team id {}", teamId);
            throw new CheckmarxException("Error occurred during team deletion");
        }
    }

    String getDescription(String session, Long scanId, Long pathId){
        GetResultDescription request = new GetResultDescription(session);
        request.setPathID(pathId);
        request.setScanID(scanId);

        log.debug("Retrieving description for {} / {} ", scanId, pathId);

        GetResultDescriptionResponse response = (GetResultDescriptionResponse)
                ws.marshalSendAndReceive(ws.getDefaultUri(), request, new SoapActionCallback(CX_WS_DESCRIPTION_URI));
        try{
            if(!response.getGetResultDescriptionResult().isIsSuccesfull()){
                log.error(response.getGetResultDescriptionResult().getErrorMessage());
                return "";
            }
            else {
                String description = response.getGetResultDescriptionResult().getResultDescription();
                description = description.replace(properties.getHtmlStrip(), "");
                description = description.replaceAll("\\<.*?>", ""); /*Strip tag elements*/
                return description;
            }
        }catch (NullPointerException e){
            log.warn("Error occurred getting description for {} / {}", scanId, pathId);
            return "";
        }
    }

    void createLdapTeamMapping(String session, Integer ldapServerId, String teamId, String teamName, String groupDn) throws CheckmarxException{
        try {
            GetTeamLdapGroupsMapping ldapReq = new GetTeamLdapGroupsMapping();

            ldapReq.setSessionId(session);
            ldapReq.setTeamId(teamId);
            log.info("Retrieving existing Ldap Group Mappings for ldap server {}", ldapServerId);
            GetTeamLdapGroupsMappingResponse ldapResponse = (GetTeamLdapGroupsMappingResponse)
                    ws.marshalSendAndReceive(ws.getDefaultUri(), ldapReq, new SoapActionCallback(CX_WS_TEAM_LDAP_MAPPINGS_URI));

            if (ldapResponse.getGetTeamLdapGroupsMappingResult().isIsSuccesfull()) {
                log.debug("Successfully retrieved ldapMappings");
                log.debug(ldapResponse.getGetTeamLdapGroupsMappingResult().getLdapGroups().getCxWSLdapGroupMapping().toString());
                CxWSLdapGroupMapping newMapping = new CxWSLdapGroupMapping();

                CxWSLdapGroup ldapGroup = new CxWSLdapGroup();
                ldapGroup.setDN(groupDn);
                LdapName ldapName = new LdapName(groupDn);
                List<Rdn> rdns = ldapName.getRdns();
                Rdn r = rdns.get(rdns.size()-1);
                String cn = r.getValue().toString();
                cn = cn.replace("CN=", "");
                cn = cn.replace("cn=", "");

                ldapGroup.setName(cn);

                newMapping.setLdapGroup(ldapGroup);
                newMapping.setLdapServerId(ldapServerId);

                ArrayOfCxWSLdapGroupMapping ldapArray = ldapResponse.getGetTeamLdapGroupsMappingResult().getLdapGroups();
                List<CxWSLdapGroupMapping> ldapGroupMapping = ldapArray.getCxWSLdapGroupMapping();
                if (!ldapGroupMapping.contains(newMapping)) {
                    ldapGroupMapping.add(newMapping);
                    UpdateTeam updateTeamReq = new UpdateTeam();
                    updateTeamReq.setSessionID(session);
                    updateTeamReq.setLdapGroupMappings(ldapArray);
                    updateTeamReq.setTeamID(teamId);
                    updateTeamReq.setNewTeamName(teamName);
                    UpdateTeamResponse updateTeamResponse = (UpdateTeamResponse)
                            ws.marshalSendAndReceive(ws.getDefaultUri(), updateTeamReq, new SoapActionCallback(CX_WS_UPDATE_TEAM_URI));
                    if (!updateTeamResponse.getUpdateTeamResult().isIsSuccesfull()) {
                        log.error("Error occurred while updating team ldap mapping {}", updateTeamResponse.getUpdateTeamResult().getErrorMessage());
                        throw new CheckmarxException("Error occurred while updating team ldap mapping {}".concat(updateTeamResponse.getUpdateTeamResult().getErrorMessage()));
                    }
                } else {
                    log.warn("Ldap mapping already exists for {} - {}", ldapServerId, groupDn);
                }

            } else {
                log.error("Error occurred while getting team ldap mapping {}", ldapResponse.getGetTeamLdapGroupsMappingResult().getErrorMessage());
                throw new CheckmarxException("Error occurred while getting team ldap mapping".concat(ldapResponse.getGetTeamLdapGroupsMappingResult().getErrorMessage()));
            }
        }catch (InvalidNameException e){
            throw new CheckmarxException("Invalid LDAP Naming ".concat(ExceptionUtils.getMessage(e)));
        }
    }

    void removeLdapTeamMapping(String session, Integer ldapServerId, String teamId, String teamName, String groupDn) throws CheckmarxException{
        try {
            GetTeamLdapGroupsMapping ldapReq = new GetTeamLdapGroupsMapping();

            ldapReq.setSessionId(session);
            ldapReq.setTeamId(teamId);
            log.info("Retrieving existing Ldap Group Mappings for ldap server {}", ldapServerId);
            GetTeamLdapGroupsMappingResponse ldapResponse = (GetTeamLdapGroupsMappingResponse)
                    ws.marshalSendAndReceive(ws.getDefaultUri(), ldapReq, new SoapActionCallback(CX_WS_TEAM_LDAP_MAPPINGS_URI));

            if (ldapResponse.getGetTeamLdapGroupsMappingResult().isIsSuccesfull()) {
                log.debug("Successfully retrieved ldapMappings");
                log.debug(ldapResponse.getGetTeamLdapGroupsMappingResult().getLdapGroups().getCxWSLdapGroupMapping().toString());
                CxWSLdapGroupMapping newMapping = new CxWSLdapGroupMapping();

                CxWSLdapGroup ldapGroup = new CxWSLdapGroup();
                ldapGroup.setDN(groupDn);
                LdapName ldapName = new LdapName(groupDn);
                List<Rdn> rdns = ldapName.getRdns();
                Rdn r = rdns.get(rdns.size()-1);
                String cn = r.getValue().toString();
                cn = cn.replace("CN=", "");
                cn = cn.replace("cn=", "");

                ldapGroup.setName(cn);

                newMapping.setLdapGroup(ldapGroup);
                newMapping.setLdapServerId(ldapServerId);

                ArrayOfCxWSLdapGroupMapping ldapArray = ldapResponse.getGetTeamLdapGroupsMappingResult().getLdapGroups();
                List<CxWSLdapGroupMapping> ldapGroupMapping = ldapArray.getCxWSLdapGroupMapping();
                if (ldapGroupMapping.contains(newMapping)) {
                    ldapGroupMapping.remove(newMapping);
                    UpdateTeam updateTeamReq = new UpdateTeam();
                    updateTeamReq.setSessionID(session);
                    updateTeamReq.setLdapGroupMappings(ldapArray);
                    updateTeamReq.setTeamID(teamId);
                    updateTeamReq.setNewTeamName(teamName);
                    UpdateTeamResponse updateTeamResponse = (UpdateTeamResponse)
                            ws.marshalSendAndReceive(ws.getDefaultUri(), updateTeamReq, new SoapActionCallback(CX_WS_UPDATE_TEAM_URI));
                    if (!updateTeamResponse.getUpdateTeamResult().isIsSuccesfull()) {
                        log.error("Error occurred while updating team ldap mapping {}", updateTeamResponse.getUpdateTeamResult().getErrorMessage());
                        throw new CheckmarxException("Error occurred while updating team ldap mapping {}".concat(updateTeamResponse.getUpdateTeamResult().getErrorMessage()));
                    }
                } else {
                    log.warn("Ldap mapping already exists for {} - {}", ldapServerId, groupDn);
                }

            } else {
                log.error("Error occurred while getting team ldap mapping {}", ldapResponse.getGetTeamLdapGroupsMappingResult().getErrorMessage());
                throw new CheckmarxException("Error occurred while getting team ldap mapping".concat(ldapResponse.getGetTeamLdapGroupsMappingResult().getErrorMessage()));
            }
        }catch (InvalidNameException e){
            throw new CheckmarxException("Invalid LDAP Naming ".concat(ExceptionUtils.getMessage(e)));
        }
    }

    Integer getLdapServerId(String session, String serverName) throws  CheckmarxException{
        GetLdapServersConfigurations request = new GetLdapServersConfigurations();
        request.setSessionId(session);

        log.debug("Retrieving Ldap Server Configurations");

        GetLdapServersConfigurationsResponse response = (GetLdapServersConfigurationsResponse)
                ws.marshalSendAndReceive(ws.getDefaultUri(), request, new SoapActionCallback(CX_WS_LDAP_CONFIGURATIONS_URI));
        try{
            if(!response.getGetLdapServersConfigurationsResult().isIsSuccesfull()){
                log.error(response.getGetLdapServersConfigurationsResult().getErrorMessage());
                throw new CheckmarxException(response.getGetLdapServersConfigurationsResult().getErrorMessage());
            }
            else {
                List<CxWSLdapServerConfiguration> ldapConfigs = response.getGetLdapServersConfigurationsResult().getServerConfigs().getCxWSLdapServerConfiguration();
                for(CxWSLdapServerConfiguration ldap: ldapConfigs){
                    if(ldap.getName().equalsIgnoreCase(serverName)){
                        return ldap.getId();
                    }
                }
                return -1;
            }
        }catch (NullPointerException e){
            log.warn("Error occurred getting ldap server configurations");
            throw new CheckmarxException("Error occurred while getting ldap server configurations");
        }
    }

}