package com.checkmarx.sdk.service;

import com.checkmarx.sdk.dto.Filter;
import com.checkmarx.sdk.dto.ScanResults;
import com.checkmarx.sdk.dto.cx.*;
import com.checkmarx.sdk.dto.cx.xml.CxXMLResultsType;
import com.checkmarx.sdk.exception.CheckmarxException;
import com.checkmarx.sdk.exception.InvalidCredentialsException;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Class used to orchestrate submitting scans and retrieving results
 */
public interface CxClient {

    /**
     * Get the last scan Id of a given project Id
     * @param projectId project Id
     * @return
     */
    public Integer getLastScanId(Integer projectId);

    /**
     * Fetches scan data based on given scan identifier, as a {@link JSONObject}.
     *
     * @param scanId scan ID to use
     * @return  populated {@link JSONObject} if scan data was fetched; empty otherwise.
     */
    public JSONObject getScanData(String scanId);

    /**
     * Fetches the Timestamp of the last full scan
     *
     * @param projectId
     * @return
     */
    public LocalDateTime getLastScanDate(Integer projectId);

    /**
     * Get the status of a given scanId
     *
     * @param scanId
     * @return
     */
    Integer getScanStatus(Integer scanId);

    /**
     * Generate a scan report request (xml) based on ScanId
     *
     * @param scanId
     * @return
     */
    Integer createScanReport(Integer scanId);

    /**
     * Get the status of a report being generated by reportId
     *
     * @param reportId
     * @return
     */
    Integer getReportStatus(Integer reportId) throws CheckmarxException;

    /**
     * Retrieve the report by scanId, mapped to ScanResults DTO, applying filtering as requested
     *
     * @param scanId
     * @param filter
     * @return
     * @throws CheckmarxException
     */
    public ScanResults getReportContentByScanId(Integer scanId, List<Filter> filter) throws CheckmarxException;

    /**
     * Retrieve the report by reportId, mapped to ScanResults DTO, applying filtering as requested
     *
     * @param reportId
     * @param filter
     * @return
     * @throws CheckmarxException
     */
    public ScanResults getReportContent(Integer reportId, List<Filter> filter) throws CheckmarxException;

    /**
     * Retrieve the xml report by reportId, mapped to ScanResults DTO, applying filtering as requested
     *
     * @param reportId
     * @return
     * @throws CheckmarxException
     */
    public CxXMLResultsType getXmlReportContent(Integer reportId) throws CheckmarxException;


    /**
     * Returns custom field values read from a Checkmarx project, based on given projectId.
     *
     * @param projectId ID of project to lookup from Checkmarx
     * @return Map of custom field names to values
     */
    public Map<String, String> getCustomFields(Integer projectId);

    /**
     * Parse CX report file, mapped to ScanResults DTO, applying filtering as requested
     *
     * @param file
     * @param filter
     * @return
     * @throws CheckmarxException
     */
    public ScanResults getReportContent(File file, List<Filter> filter) throws CheckmarxException;

    /**
     * @param vulnsFile
     * @param libsFile
     * @param filter
     * @return
     * @throws CheckmarxException
     */
    public ScanResults getOsaReportContent(File vulnsFile, File libsFile, List<Filter> filter) throws CheckmarxException;


    public String getIssueDescription(Long scanId, Long pathId);

    /**
     * Create Project under a given Owner Id team
     *
     * @param ownerId
     * @param name
     * @return Id of the new project
     */
    public Integer createProject(String ownerId, String name);

    /**
     * Delete a project by Id
     * @param projectId
     */
    public void deleteProject(Integer projectId);

    /**
     * Get All Projects in Checkmarx
     *
     * @return
     */
    public List<CxProject> getProjects() throws CheckmarxException;

    /**
     * Get All Projects in Checkmarx
     *
     * @return
     */
    public List<CxProject> getProjects(String teamId) throws CheckmarxException;

    /**
     * Get All Projects under a specific team within Checkmarx
     * <p>
     * using TeamId does not work.
     *
     * @param ownerId
     * @return
     */

    public Integer getProjectId(String ownerId, String name);

    /**
     * Return Project based on projectId
     *
     * @return
     */
    public CxProject getProject(Integer projectId);


    /**
     * Check if a scan exists for a projectId
     *
     * @param projectId
     * @return
     */
    public boolean scanExists(Integer projectId);

    /**
     * Create Scan Settings
     *
     * @param projectId
     * @param presetId
     * @param engineConfigId
     * @return
     */
    public Integer createScanSetting(Integer projectId, Integer presetId, Integer engineConfigId);

    /**
     * Get Scan Settings for an existing project (JSON String)
     *
     * @param projectId
     * @return
     */
    public String getScanSetting(Integer projectId);

    /**
     * Get Preset Name based on Id
     *
     * @param presetId
     * @return
     */
    public String getPresetName(Integer presetId);


    /**
     * Get Preset Id of an existing project
     *
     * @param projectId
     * @return
     */
    public Integer getProjectPresetId(Integer projectId);

    /**
     * Set Repository details for a project
     *
     * @param projectId
     * @param gitUrl
     * @param branch
     * @throws CheckmarxException
     */
    public void setProjectRepositoryDetails(Integer projectId, String gitUrl, String branch) throws CheckmarxException;

    /**
     * Upload file (zip of source) for a project
     *
     * @param projectId
     * @param file
     * @throws CheckmarxException
     */
    public void uploadProjectSource(Integer projectId, File file) throws CheckmarxException;

    /**
     *
     * @param projectId Id of Checkmarx Project
     * @param excludeFolders list of folder exclusions to apply to a scan
     * @param excludeFiles list of file exclusions to apply to a scan
     */
    public void setProjectExcludeDetails(Integer projectId, List<String> excludeFolders, List<String> excludeFiles);

    /**
     *
     * @param ldapServerId
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public Integer getLdapTeamMapId(Integer ldapServerId, String teamId, String ldapGroupDn) throws CheckmarxException;

    /**
     * Get teamId for given path
     *
     * @param teamPath
     * @return
     * @throws CheckmarxException
     */
    public String getTeamId(String teamPath) throws CheckmarxException;

    /**
     * Fetches all teams
     *
     * @return  a List containing the Teams in Checkmarx
     * @throws CheckmarxException
     */
    public List<CxTeam> getTeams() throws CheckmarxException;

    /**
     * Adds an LDAP team association - uses SOAP Web Service
     * @param ldapServerId
     * @param teamId
     * @param teamName
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public void mapTeamLdap(Integer ldapServerId, String teamId, String teamName, String ldapGroupDn) throws CheckmarxException;

    /**
     * Retrieve LDAP team mapping associations
     * @param ldapServerId
     * @throws CheckmarxException
     */
    public List<CxTeamLdap> getTeamLdap(Integer ldapServerId) throws CheckmarxException;

    /**
     * Removes an LDAP team association - uses SOAP Web Service
     *
     * @param ldapServerId
     * @param teamId
     * @param teamName can be null/empty if using 9.0.  Only applicable to 8.x
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public void removeTeamLdap(Integer ldapServerId, String teamId, String teamName, String ldapGroupDn) throws CheckmarxException;

    /**
     * Returns a list of roles in Checkmarx
     * @return
     */
    public List<CxRole> getRoles() throws CheckmarxException;

    /**
     * Returns the Id of an associated role in Checkmarx
     * @param roleName
     */
    public Integer getRoleId(String roleName) throws CheckmarxException;

    /**
     * Retrieve list of Role LDAP mappings associated with an LDAP server Id
     * @param ldapServerId
     * @return
     * @throws CheckmarxException
     */
    public List<CxRoleLdap> getRoleLdap(Integer ldapServerId) throws CheckmarxException;

    /**
     * Retrieve the Id of a role mapping associated with an LDAP Group DN
     * @param ldapServerId
     * @param ldapGroupDn
     * @return
     * @throws CheckmarxException
     */
    public Integer getLdapRoleMapId(Integer ldapServerId, Integer roleId, String ldapGroupDn) throws CheckmarxException;

    /**
     * @param ldapServerId
     * @param roleId
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public void mapRoleLdap(Integer ldapServerId, Integer roleId, String ldapGroupDn) throws CheckmarxException;

    /**
     * Removes a role/ldap mapping association
     * @param roleMapId
     * @throws CheckmarxException
     */
    public void removeRoleLdap(Integer roleMapId) throws CheckmarxException;

    /**
     * Removes a role/ldap mapping association
     * @param ldapServerId
     * @param roleId
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public void removeRoleLdap(Integer ldapServerId, Integer roleId, String ldapGroupDn) throws CheckmarxException;

    /**
     * Adds an LDAP team association - uses SOAP Web Service
     * @param ldapServerId
     * @param teamId
     * @param teamName
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public void mapTeamLdapWS(Integer ldapServerId, String teamId, String teamName, String ldapGroupDn) throws CheckmarxException;

    /**
     * Removes an LDAP team association - uses SOAP Web Service
     *
      * @param ldapServerId
     * @param teamId
     * @param teamName
     * @param ldapGroupDn
     * @throws CheckmarxException
     */
    public void removeTeamLdapWS(Integer ldapServerId, String teamId, String teamName, String ldapGroupDn) throws CheckmarxException;


    /**
     * Create team under given parentId
     *
     * @param parentTeamId
     * @param teamName
     * @return new TeamId
     * @throws CheckmarxException
     */
    public String createTeamWS(String parentTeamId, String teamName) throws CheckmarxException;

    /**
     * Create team under given parentId - Will use REST API to create team for version 9.0+
     *
     * @param parentTeamId
     * @param teamName
     * @return new TeamId
     * @throws CheckmarxException
     */
    public String createTeam(String parentTeamId, String teamName) throws CheckmarxException;

    /**
     * Create team under given parentId - Will use REST API to create team for version 9.0+
     *
     * @param teamId
     * @return
     * @throws CheckmarxException
     */
    public void deleteTeam(String teamId) throws CheckmarxException;

    /**
     * Get a team Id based on the name and the Parent Team Id
     * @param parentTeamId
     * @param teamName
     * @return
     * @throws CheckmarxException
     */
    public String getTeamId(String parentTeamId, String teamName) throws CheckmarxException;


        /**
         * Delete a team based on the name for a given parent team id
         *
         * @param teamId
         * @return
         * @throws CheckmarxException
         */
    public void deleteTeamWS(String teamId) throws CheckmarxException;

    /**
     * Get scan configuration Id
     *
     * @param configuration
     * @return
     * @throws CheckmarxException
     */
    public Integer getScanConfiguration(String configuration) throws CheckmarxException;

    /**
     * Fetch the Id of a given preset name
     *
     * @param preset name of the preset to find the Id for
     * @return Id for the scan configuration
     * @throws CheckmarxException
     */
    public Integer getPresetId(String preset) throws CheckmarxException;

    /**
     * Get scan summary for given scanId
     *
     * @param scanId
     * @return Id for the preset
     * @throws CheckmarxException
     */
    public CxScanSummary getScanSummaryByScanId(Integer scanId) throws CheckmarxException;

    /**
     * Get scan summary for the latest scan of a given project Id
     *
     * @param projectId project Id to retrieve the latest scan summary for
     * @return CxScanSummary containing scan summary information
     * @throws CheckmarxException
     */
    public CxScanSummary getScanSummary(Integer projectId) throws CheckmarxException;

    /**
     * Get scan summary for the latest scan associated with a teamName & projectName
     *
     * @param teamName
     * @param projectName
     * @return CxScanSummary containing scan summary information
     * @throws CheckmarxException
     */
    public CxScanSummary getScanSummary(String teamName, String projectName) throws CheckmarxException;

    /**
     * Create a scan based on the CxScanParams and return the scan Id
     *
     * @param params attributes used to define the project
     * @param comment
     * @return Scan Id associated with the new scan
     * @throws CheckmarxException
     */
    public Integer createScan(CxScanParams params, String comment) throws CheckmarxException;

    /**
     * Wait for the scan of a given scan Id to finish
     *
     * @param scanId
     * @throws CheckmarxException
     */
    public void waitForScanCompletion(Integer scanId) throws CheckmarxException;

    /**
     *
     * @param scanId
     * @return
     * @throws CheckmarxException
     */
    public void deleteScan(Integer scanId) throws CheckmarxException;

    /**
     * Create a scan based on the CxScanParams and wait for the scan to complete, returning the result XML Jaxb object
     *
     * @param params attributes used to define the project
     * @param comment
     * @return CxXMLResultType (Jaxb/XML object representation of the scan results)
     * @throws CheckmarxException
     */
    public CxXMLResultsType createScanAndReport(CxScanParams params, String comment) throws CheckmarxException;

    /**
     * Create a scan based on the CxScanParams and return the ScanResults object based on filters
     * @param params attributes used to define the project
     * @param comment
     * @param filters filters to apply to the scan result set (severity, category, cwe)
     * @return
     * @throws CheckmarxException
     */
    public ScanResults createScanAndReport(CxScanParams params, String comment, List<Filter> filters) throws CheckmarxException;

    /**
     * Create a scan based on the CxScanParams and wait for the scan to complete, returning the result XML Jaxb object
     *
     * @param teamName
     * @param projectName
     * @return
     * @throws CheckmarxException
     */
    public CxXMLResultsType getLatestScanReport(String teamName, String projectName) throws CheckmarxException;

    /**
     * Create a scan based on the CxScanParams and return the ScanResults object based on filters
     *
     * @param teamName
     * @param projectName
     * @param filters
     * @return
     * @throws CheckmarxException
     */
    public ScanResults getLatestScanResults(String teamName, String projectName, List<Filter> filters) throws CheckmarxException;

    public Integer getLdapServerId(String serverName) throws  CheckmarxException;

    //TODO Engine Management

}
