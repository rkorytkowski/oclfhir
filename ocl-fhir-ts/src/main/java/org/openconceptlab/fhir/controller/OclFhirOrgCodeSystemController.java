package org.openconceptlab.fhir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.openconceptlab.fhir.util.OclFhirConstants.*;
import static org.openconceptlab.fhir.util.OclFhirConstants.VALIDATE_CODE;
import static org.openconceptlab.fhir.util.OclFhirUtil.*;
import static org.openconceptlab.fhir.util.OclFhirUtil.newStringType;

@Tag(name = CODE_SYSTEM_ORGANIZATION_NAMESPACE, description = "organization namespace")
@RestController
@RequestMapping({"/orgs/{org}/CodeSystem"})
public class OclFhirOrgCodeSystemController extends BaseOclFhirController {

    public OclFhirOrgCodeSystemController(CodeSystemResourceProvider codeSystemResourceProvider,
                                          ValueSetResourceProvider valueSetResourceProvider,
                                          OclFhirUtil oclFhirUtil) {
        super(codeSystemResourceProvider, valueSetResourceProvider, oclFhirUtil);
    }

    /**
     * Create {@link CodeSystem}
     *
     * @param org        - the organization id
     * @param codeSystem - the {@link CodeSystem} resource
     * @return ResponseEntity
     */
    @Operation(summary = CREATE_CODESYSTEM)
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> createCodeSystemForOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestBody @Parameter(description = THE_CODESYSTEM_JSON_RESOURCE) String codeSystem,
                                                         @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        Optional<Identifier> acsnOpt = hasAccessionIdentifier(system.getIdentifier());
        ResponseEntity<String> response = validate(system.getIdElement().getIdPart(), acsnOpt, ORGS, org);
        if (response != null) return response;
        if (acsnOpt.isEmpty()) addIdentifier(
                system.getIdentifier(), ORGS, org, CODESYSTEM, system.getIdElement().getIdPart(), system.getVersion());

        performCreate(system, auth);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Update {@link CodeSystem} version.
     *
     * @param id         - the {@link CodeSystem} id
     * @param version    - the {@link CodeSystem} version
     * @param org        - the organization id
     * @param codeSystem - the {@link CodeSystem} resource
     * @return ResponseEntity
     */
    @Operation(summary = UPDATE_CODESYSTEM_VERSION)
    @PutMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> updateCodeSystemForOrg(@PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                         @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                         @PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestBody @Parameter(description = THE_CODESYSTEM_JSON_RESOURCE) String codeSystem,
                                                         @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, ORG, org))
            return badRequest("The CodeSystem can not be edited.");
        CodeSystem system = (CodeSystem) parser.parseResource(codeSystem);
        IdType idType = new IdType(CODESYSTEM, id, version);
        performUpdate(system, auth, idType, formatOrg(org));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Delete {@link CodeSystem} version.
     *
     * @param id      - the {@link CodeSystem} id
     * @param version - the {@link CodeSystem} version
     * @param org     - the organization id
     * @return ResponseEntity
     */
    @Operation(summary = DELETE_CODESYSTEM_VERSION)
    @DeleteMapping(path = {"/{id}/version/{version}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteCodeSystemByOrg(@PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                        @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                        @PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                        @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        if (!validateIfEditable(CODESYSTEM, id, version, ORG, org)) return badRequest("The CodeSystem can not be deleted.");
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + SOURCES + FS + id + FS + version + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Delete concepts from {@link CodeSystem} version.
     *
     * @param id        - the {@link CodeSystem} id
     * @param version   - the {@link CodeSystem} version
     * @param conceptId - the concept code
     * @param org       - the organization id
     * @return ResponseEntity
     */
    @Operation(summary = DELETE_CONCEPT_FROM_CODESYSTEM_VERSION)
    @DeleteMapping(path = {"/{id}/version/{version}/concepts/{concept_id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConceptInCodeSystemForOrg(@PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                  @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                                  @PathVariable(name = CONCEPT_ID) @Parameter(description = THE_CONCEPT_CODE) String conceptId,
                                                                  @PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                  @RequestHeader(name = AUTHORIZATION) @Parameter(hidden = true) String auth) {
        String url = oclFhirUtil.oclApiBaseUrl() + FS + ORGS + FS + org + FS + SOURCES + FS + id + FS + version + FS + CONCEPTS + FS + conceptId + FS;
        return performDeleteOclApi(url, auth);
    }

    /**
     * Read {@link CodeSystem} by Id.
     *
     * @param org  - the organization id
     * @param id   - the {@link CodeSystem} id
     * @param page - the page number
     * @return ResponseEntity
     */
    @Operation(summary = GET_CODESYSTEM_BY_ORGANIZATION_AND_ID)
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                     @PathVariable @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                     @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                     HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read {@link CodeSystem} version.
     *
     * @param org     - the organization id
     * @param id      - the {@link CodeSystem} id
     * @param version - the {@link CodeSystem} version
     * @param page    - the page number
     * @return ResponseEntity
     */
    @Operation(summary = GET_SEARCH_CODESYSTEM_VERSIONS)
    @GetMapping(path = {"/{id}/version", "/{id}/version/{version}"},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCodeSystemVersionsByOrg(@PathVariable(name = ORG) @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                             @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                             @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) Optional<String> version,
                                                             @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                             HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), ID, id, VERSION, version.orElse(ALL),
                PAGE, page.orElse("1"), OWNER_URL, getRequestUrl(request));
    }

    /**
     * Read all {@link CodeSystem} of org.
     *
     * @param org  - the organization id
     * @param page - the page number
     * @return ResponseEntity
     */
    @Operation(summary = SEARCH_CODESYSTEMS_FOR_ORGANIZATION)
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> searchCodeSystemsByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestParam(name = PAGE, required = false) Optional<String> page,
                                                         HttpServletRequest request) {
        return handleSearchResource(CodeSystem.class, OWNER, formatOrg(org), PAGE, page.orElse("1"),
                OWNER_URL, getRequestUrl(request));
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org             - the organization id
     * @param system          - the {@link CodeSystem} url
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_LOOKUP_BY_URL)
    @GetMapping(path = {"/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestParam(name = SYSTEM) @Parameter(description = THE_CODESYSTEM_URL) String system,
                                                         @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                         @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                         @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DISPLAY_PARAMETER) String displayLanguage) {
        Parameters parameters = lookupParameters(system, code, version, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = CODESYSTEM_LOOKUP_REQ_BODY_EXAMPLE)
            })
    })
    @Operation(summary = PERFORM_LOOKUP_BY_URL)
    @PostMapping(path = {"/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                         @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org             - the organization id
     * @param url             - the {@link CodeSystem} url
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param display         - the concept display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @GetMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                           @RequestParam(name = URL) @Parameter(description = THE_CODESYSTEM_URL) String url,
                                                           @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                           @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                           @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
                                                           @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(url, code, version, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_EXAMPLE1),
                    @ExampleObject(name = "example2", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_EXAMPLE2)
            })
    })
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_URL)
    @PostMapping(path = {"/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrg(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                           @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org             - the organization id
     * @param id              - the {@link CodeSystem} id
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_LOOKUP_BY_ID)
    @GetMapping(path = {"/{id}/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrgAndId(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                              @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                              @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                              @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                              @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DISPLAY_PARAMETER) String displayLanguage) {
        Parameters parameters = lookupParameters(EMPTY, code, version, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP, id);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org             - the organization id
     * @param id              - the {@link CodeSystem} id
     * @param code            - the concept code
     * @param pathVersion         - the {@link CodeSystem} version
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_LOOKUP_BY_ID)
    @GetMapping(path = {"/{id}/version/{version}/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrgAndIdAndVersion(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                        @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                        @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String pathVersion,
                                                                        @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                                        @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DISPLAY_PARAMETER) String displayLanguage) {
        Parameters parameters = lookupParameters(EMPTY, code, pathVersion, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, LOOKUP, id);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org        - the organization id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = CODESYSTEM_LOOKUP_REQ_BODY_ID_EXAMPLE)
            })
    })
    @Operation(summary = PERFORM_LOOKUP_BY_ID)
    @PostMapping(path = {"/{id}/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrgAndId(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                              @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                              @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, LOOKUP, id);
    }

    /**
     * Perform {@link CodeSystem} $lookup.
     *
     * @param org        - the organization id
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(value = CODESYSTEM_LOOKUP_REQ_BODY_ID_VERSION_EXAMPLE)
            })
    })
    @Operation(summary = PERFORM_LOOKUP_BY_ID)
    @PostMapping(path = {"/{id}/version/{version}/$lookup"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> lookUpCodeSystemsByOrgAndIdAndVersion(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                        @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                        @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String pathVersion,
                                                                        @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        if (isValid(pathVersion)) params.setParameter(VERSION, pathVersion);
        return handleFhirOperation(params, CodeSystem.class, LOOKUP, id);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org             - the organization id
     * @param id              - the {@link CodeSystem} id
     * @param code            - the concept code
     * @param version         - the {@link CodeSystem} version
     * @param display         - the concept display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @GetMapping(path = {"/{id}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrgAndId(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                                @RequestParam(name = VERSION, required = false) @Parameter(description = THE_CODESYSTEM_VERSION) String version,
                                                                @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
                                                                @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(EMPTY, code, version, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org             - the organization id
     * @param id              - the {@link CodeSystem} id
     * @param code            - the concept code
     * @param pathVersion         - the {@link CodeSystem} version
     * @param display         - the concept display
     * @param displayLanguage - the display language
     * @return ResponseEntity
     */
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @GetMapping(path = {"/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrgAndIdAndVersion(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                          @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                          @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String pathVersion,
                                                                          @RequestParam(name = CODE) @Parameter(description = THE_CONCEPT_CODE) String code,
                                                                          @RequestParam(name = DISPLAY, required = false) @Parameter(description = THE_DISPLAY_ASSOCIATED_WITH_THE_CODE) String display,
                                                                          @RequestParam(name = DISP_LANG, required = false) @Parameter(description = THE_LANGUAGE_TO_BE_USED_FOR_DESCRIPTION_WHEN_VALIDATING_THE_DISPLAY_PROPERTY) String displayLanguage) {
        Parameters parameters = codeSystemVCParameters(EMPTY, code, pathVersion, display, displayLanguage, formatOrg(org));
        return handleFhirOperation(parameters, CodeSystem.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org        - the organization id
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE1),
                    @ExampleObject(name = "example2", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_EXAMPLE2)
            })
    })
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @PostMapping(path = {"/{id}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrgAndId(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE, id);
    }

    /**
     * Perform {@link CodeSystem} $validate-code.
     *
     * @param org        - the organization id
     * @param id         - the {@link CodeSystem} id
     * @param parameters - the input parameters
     * @return ResponseEntity
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "example1", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE1),
                    @ExampleObject(name = "example2", value = CODESYSTEM_VALIDATE_CODE_REQ_BODY_ID_VERSION_EXAMPLE2)
            })
    })
    @Operation(summary = PERFORM_VALIDATE_CODE_BY_ID)
    @PostMapping(path = {"/{id}/version/{version}/$validate-code"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> validateCodeSystemsByOrgAndIdAndVersion(@PathVariable @Parameter(description = THE_ORGANIZATION_ID) String org,
                                                                          @PathVariable(name = ID) @Parameter(description = THE_CODESYSTEM_ID) String id,
                                                                          @PathVariable(name = VERSION) @Parameter(description = THE_CODESYSTEM_VERSION) String pathVersion,
                                                                          @RequestBody @Parameter(description = THE_FHIR_PARAMETERS_OBJECT) String parameters) {
        Parameters params = (Parameters) getResource(parameters);
        params.addParameter().setName(OWNER).setValue(newStringType(formatOrg(org)));
        if (isValid(pathVersion)) params.setParameter(VERSION, pathVersion);
        return handleFhirOperation(params, CodeSystem.class, VALIDATE_CODE, id);
    }

}

