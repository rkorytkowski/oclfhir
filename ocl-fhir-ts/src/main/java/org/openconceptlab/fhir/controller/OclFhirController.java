package org.openconceptlab.fhir.controller;

import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.*;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * The OclFhirController class. This is used to support OCL compatible end points.
 *
 * @author harpatel1
 */
@RestController
@RequestMapping({"/"})
public class OclFhirController {

    CodeSystemResourceProvider codeSystemResourceProvider;
    ValueSetResourceProvider valueSetResourceProvider;
    OclFhirUtil oclFhirUtil;

    @Autowired
    public OclFhirController(CodeSystemResourceProvider codeSystemResourceProvider,
                             ValueSetResourceProvider valueSetResourceProvider,
                             OclFhirUtil oclFhirUtil) {
        this.codeSystemResourceProvider = codeSystemResourceProvider;
        this.valueSetResourceProvider = valueSetResourceProvider;
        this.oclFhirUtil = oclFhirUtil;
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByOrg(@PathVariable(name = ORG) String org, @PathVariable(name = ID) String id) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/{id}/version",
                        "/orgs/{org}/CodeSystem/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) String org,
                                                             @PathVariable(name = ID) String id,
                                                             @PathVariable(name = VERSION) Optional<String> version) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByOrg(@PathVariable String org) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org,
                                         @RequestParam(name = SYSTEM) String system,
                                         @RequestParam(name = CODE) String code,
                                         @RequestParam(name = VERSION, required = false) String version,
                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    @PostMapping(path = {"/orgs/{org}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    @GetMapping(path = {"/orgs/{org}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org,
                                                           @RequestParam(name = URL) String url,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = DISPLAY, required = false) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = validateCodeParameters(url, code, version, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/orgs/{org}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable String org, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByOrg(@PathVariable String org, @PathVariable String id) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id);
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet/{id}/version",
                        "/orgs/{org}/ValueSet/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByOrg(@PathVariable(name = ORG) String org,
                                                           @PathVariable(name = ID) String id,
                                                           @PathVariable(name = VERSION) Optional<String> version) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/orgs/{org}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByOrg(@PathVariable String org) {
        return handleSearchResource(ValueSet.class, OWNER, formatOrg(org));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByUser(@PathVariable String user, @PathVariable String id) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/{id}/version",
                        "/users/{user}/CodeSystem/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByUser(@PathVariable(name = USER) String user,
                                                              @PathVariable(name = ID) String id,
                                                              @PathVariable(name = VERSION) Optional<String> version) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByUser(@PathVariable String user) {
        return handleSearchResource(CodeSystem.class, OWNER, formatUser(user));
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user,
                                                         @RequestParam(name = SYSTEM) String system,
                                                         @RequestParam(name = CODE) String code,
                                                         @RequestParam(name = VERSION, required = false) String version,
                                                         @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    @PostMapping(path = {"/users/{user}/CodeSystem/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    @GetMapping(path = {"/users/{user}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user,
                                                           @RequestParam(name = URL) String url,
                                                           @RequestParam(name = CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) String version,
                                                           @RequestParam(name = DISPLAY, required = false) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) String displayLanguage) {
        Parameters parameters = validateCodeParameters(url, code, version, display, displayLanguage, formatUser(user));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    @PostMapping(path = {"/users/{user}/CodeSystem/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByUser(@PathVariable String user, @RequestBody String parameters){
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatUser(user)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetByUser(@PathVariable String user, @PathVariable String id) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id);
    }

    @GetMapping(path = {"/users/{user}/ValueSet/{id}/version",
                        "/users/{user}/ValueSet/{id}/version/{version}"},
                produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getValueSetVersionsByUser(@PathVariable(name = USER) String user,
                                                            @PathVariable(name = ID) String id,
                                                            @PathVariable(name = VERSION) Optional<String> version) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user), ID, id, VERSION, version.orElse(ALL));
    }

    @GetMapping(path = {"/users/{user}/ValueSet"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchValueSetsByUser(@PathVariable String user) {
        return handleSearchResource(ValueSet.class, OWNER, formatUser(user));
    }

    private ResponseEntity<String> handleSearchResource(final Class<? extends MetadataResource> resourceClass, final String... args) {
        try {
            String resource = searchResource(resourceClass, args);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    private ResponseEntity<String> handleFhirOperation(Parameters parameters, Class<? extends Resource> type, String operation) {
        try {
            return ResponseEntity.ok(oclFhirUtil.getResourceAsString(performFhirOperation(parameters, type, operation)));
        } catch (ResourceNotFoundException e) {
            return notFound(e.getStatusCode(), e.getResponseBody());
        } catch (Exception e) {
            return badRequest();
        }
    }

    private String searchResource(final Class<? extends MetadataResource> resourceClass, final String... filters) {
        IQuery q = oclFhirUtil.getClient().search().forResource(resourceClass);
        if (filters.length % 2 == 0) {
            for (int i = 0; i < filters.length; i += 2) {
                if (i == 0) {
                    q = q.where(new StringClientParam(filters[i]).matches().value(filters[i + 1]));
                } else {
                    q = q.and(new StringClientParam(filters[i]).matches().value(filters[i + 1]));
                }
            }
        }
        Bundle bundle = (Bundle) q.execute();
        return oclFhirUtil.getResourceAsString(bundle);
    }

    private Parameters performFhirOperation(Parameters parameters, Class<? extends Resource> type, String operation) {
        return oclFhirUtil.getClient()
                .operation()
                .onType(type)
                .named(operation)
                .withParameters(parameters)
                .execute();
    }

    private Parameters generateParameters(String code, String version, String displayLanguage, String owner) {
        Parameters parameters = new Parameters();
        parameters.addParameter().setName(CODE).setValue(new CodeType(code));
        if (isValid(version))
            parameters.addParameter().setName(VERSION).setValue(newStringType(version));
        if (isValid(displayLanguage))
            parameters.addParameter().setName(DISP_LANG).setValue(new CodeType(displayLanguage));
        parameters.addParameter().setName(OWNER).setValue(newStringType(owner));
        return parameters;
    }

    private Parameters lookupParameters(String system, String code, String version, String displayLanguage, String owner) {
        Parameters parameters = generateParameters(code, version, displayLanguage, owner);
        parameters.addParameter().setName(SYSTEM).setValue(new UriType(system));
        return parameters;
    }

    private Parameters validateCodeParameters(String url, String code, String version, String display, String displayLanguage,
                                              String owner) {
        Parameters parameters = generateParameters(code, version, displayLanguage, owner);
        parameters.addParameter().setName(URL).setValue(new UriType(url));
        if (isValid(display))
            parameters.addParameter().setName(DISPLAY).setValue(newStringType(display));
        return parameters;
    }

    private static String formatOrg(String org) {
        return ORG_ + org;
    }

    private static String formatUser(String user) {
        return USER_ + user;
    }
}
