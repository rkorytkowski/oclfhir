package org.openconceptlab.fhir.provider;

import static org.mockito.Mockito.*;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.openconceptlab.fhir.base.OclFhirTest;
import org.openconceptlab.fhir.model.*;
import org.openconceptlab.fhir.provider.ValueSetResourceProvider;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TestValueSetResourceProvider extends OclFhirTest {

    Collection collection1;
    Collection collection2;

    public static final String URL_COLLECTION_1 = "http://openconceptlab.org/collection1";
    public static final String COLLECTION_1 = "collection1";
    public static final String COLLECTION_1_NAME = "collection1 name";
    public static final String COLLECTION_1_FULL_NAME = "collection1 full name";
    public static final String COLLECTION_1_COPYRIGHT_TEXT = "collection1 copyright text";
    public static final String TEST = "TEST";

    public static final String URL_COLLECTION_2 = "http://openconceptlab.org/collection2";
    public static final String COLLECTION_2 = "collection2";
    public static final String COLLECTION_2_NAME = "collection2 name";
    public static final String COLLECTION_2_FULL_NAME = "collection2 full name";
    public static final String COLLECTION_2_COPYRIGHT_TEXT = "collection2 copyright text";

    @Before
    public void setUpBefore() {
        MockitoAnnotations.initMocks(this);
        source1 = source(123L, "v1.0", concept1(), concept2(), concept3());
        source2 = source(234L, "v2.0", concept1(), concept2(), concept3(), concept4());
        source3 = source(345L, "v3.0", concept1(), concept2(), concept3(), concept4());
        populateSource1(source1);
        populateSource2(source2);

        collection1 = new Collection();
        populateCollection1(collection1);
        collection2 = new Collection();
        populateCollection2(collection2);
    }

    @After
    public void after() {
        source1 = null;
        source2 = null;
        source3 = null;
        collection1 = null;
        collection2 = null;
        cs11 = null;
        cs21 = null;
        cs22 = null;
        cs23 = null;
        cs24 = null;
        cs31 = null;
        cs32 = null;
        cs33 = null;
        cs34 = null;
    }

    private void populateCollection2(Collection collection2) {
        collection2.setMnemonic(COLLECTION_2);
        collection2.setUri(URL_COLLECTION_2);
        collection2.setCanonicalUrl(URL_COLLECTION_2);
        collection2.setName(COLLECTION_2_NAME);
        collection2.setFullName(COLLECTION_2_FULL_NAME);
        collection2.setDescription(COLLECTION_2_NAME);
        collection2.setIsActive(false);
        collection2.setPublisher(TEST);
        collection2.setContact("[{\"name\": \"Jon Doe 2\", \"telecom\": [{\"use\": \"work\", \"rank\": 1, \"value\": \"jondoe2@gmail.com\", \"period\": {\"end\": \"2022-10-29T10:26:15-04:00\", \"start\": \"2021-10-29T10:26:15-04:00\"}, \"system\": \"email\"}]}]");
        collection2.setJurisdiction("[{\"coding\": [{\"code\": \"ETH\", \"system\": \"http://unstats.un.org/unsd/methods/m49/m49.htm\", \"display\": \"Ethiopia\"}]}]");
        collection2.setPurpose(TEST);
        collection2.setCopyright(COLLECTION_2_COPYRIGHT_TEXT);
        collection2.setImmutable(true);
        collection2.setRevisionDate(Date.from(LocalDate.of(2020, 12, 2).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void populateCollection1(Collection collection1) {
        collection1.setMnemonic(COLLECTION_1);
        collection1.setUri(URL_COLLECTION_1);
        collection1.setCanonicalUrl(URL_COLLECTION_1);
        collection1.setName(COLLECTION_1_NAME);
        collection1.setFullName(COLLECTION_1_FULL_NAME);
        collection1.setDescription(COLLECTION_1_NAME);
        collection1.setIsActive(true);
        collection1.setPublisher(TEST);
        collection1.setContact("[{\"name\": \"Jon Doe 1\", \"telecom\": [{\"use\": \"work\", \"rank\": 1, \"value\": \"jondoe1@gmail.com\", \"period\": {\"end\": \"2025-10-29T10:26:15-04:00\", \"start\": \"2020-10-29T10:26:15-04:00\"}, \"system\": \"email\"}]}]");
        collection1.setJurisdiction("[{\"coding\": [{\"code\": \"USA\", \"system\": \"http://unstats.un.org/unsd/methods/m49/m49.htm\", \"display\": \"United States of America\"}]}]");
        collection1.setPurpose(TEST);
        collection1.setCopyright(COLLECTION_1_COPYRIGHT_TEXT);
        collection1.setImmutable(false);
        collection1.setRevisionDate(Date.from(LocalDate.of(2020, 12, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/source1/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/source2/v2.0/concepts/"+TUMOR_DISORDER+"/123/"
        );
        collection1.setCollectionsReferences(references);
    }

    @Test
    public void testSearchValueSet_return_1() {
        collection1.setReleased(true);
        when(collectionRepository.findAllMostRecentReleased(anyList())).thenReturn(Collections.singletonList(collection1));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSets(requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertEquals(0, valueSet.getCompose().getInclude().size());
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
    }

    @Test
    public void testSearchValueSet_return_2() {
        collection1.setReleased(true);
        collection2.setReleased(true);
        when(collectionRepository.findAllMostRecentReleased(anyList())).thenReturn(Arrays.asList(collection1, collection2));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSets(requestDetails);
        assertEquals(2, bundle.getTotal());
        ValueSet valueSet1 = (ValueSet) bundle.getEntry().get(0).getResource();
        assertEquals(0, valueSet1.getCompose().getInclude().size());
        assertBaseValueSet(valueSet1, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        ValueSet valueSet2 = (ValueSet) bundle.getEntry().get(1).getResource();
        assertEquals(0, valueSet2.getCompose().getInclude().size());
        assertBaseValueSet(valueSet2, URL_COLLECTION_2, COLLECTION_2_NAME, COLLECTION_2_FULL_NAME, "Jon Doe 2", "jondoe2@gmail.com",
                "ETH", TEST, COLLECTION_2_COPYRIGHT_TEXT);
    }

    @Test
    public void testSearchValueSet_return_empty() {
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSets(requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchValueSet_head_return_empty() {
        collection1.setIsLatestVersion(true);
        collection2.setIsLatestVersion(true);
        collection1.setVersion("HEAD");
        collection2.setVersion("HEAD");
        when(collectionRepository.findByPublicAccessIn(anyList())).thenReturn(Arrays.asList(collection1, collection2));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSets(requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test
    public void testSearchValueSetByUrl_version_empty_return_most_recent() {
        when(collectionRepository.findFirstByCanonicalUrlAndReleasedAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
        .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByUrl(newString(URL_COLLECTION_1), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(0), URL_SOURCE_2, V_2_0, TM, TUMOR_DISORDER);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(1), URL_SOURCE_1, V_1_0, AD, ALLERGIC_DISORDER);
    }

    @Test
    public void testSearchValueSetByUrl_version() {
        when(collectionRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(anyString(), anyString(), anyList()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByUrl(newString(URL_COLLECTION_1), newString(V_1_0), null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(0), URL_SOURCE_2, V_2_0, TM, TUMOR_DISORDER);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(1), URL_SOURCE_1, V_1_0, AD, ALLERGIC_DISORDER);
    }

    @Test
    public void testSearchValueSetByUrl_version_all() {
        collection1.setVersion(V_1_0);
        Collection collection1V2 = new Collection();
        populateCollection1(collection1V2);
        collection1V2.setVersion("v1.1");
        when(collectionRepository.findByCanonicalUrlAndPublicAccessIn(anyString(), anyList()))
                .thenReturn(Arrays.asList(collection1, collection1V2));
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByUrl(newString(URL_COLLECTION_1), newString("*"), null, requestDetails);
        assertEquals(2, bundle.getTotal());
        ValueSet valueSet1 = (ValueSet) bundle.getEntry().get(0).getResource();
        ValueSet valueSet2 = (ValueSet) bundle.getEntry().get(1).getResource();
        assertEquals(V_1_0, valueSet1.getVersion());
        assertEquals("v1.1", valueSet2.getVersion());
    }

    @Test
    public void testSearchValueSetByUrl_version_head() {
        when(collectionRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(anyString(), anyString(), anyList()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        collection1.setVersion("HEAD");
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByUrl(newString(URL_COLLECTION_1), newString(V_1_0), null, requestDetails);
        assertEquals(0, bundle.getTotal());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testSearchValueSetByUrl_not_found() {
        ValueSetResourceProvider provider = valueSetProvider();
        provider.searchValueSetByUrl(newString(URL_COLLECTION_1), null, null, requestDetails);
    }

    @Test
    public void testSearchValueSetByOwner() {
        collection1.setReleased(true);
        when(collectionRepository.findByOrganizationMnemonicAndPublicAccessIn(anyString(), anyList()))
                .thenReturn(Collections.singletonList(collection1));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwner(newString("org:OCL"), requestDetails);
        assertEquals(1, bundle.getTotal());
        assertBaseValueSet((ValueSet) bundle.getEntryFirstRep().getResource(), URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(0, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
    }

    @Test
    public void testSearchValueSetByOwner_user() {
        collection1.setReleased(true);
        when(collectionRepository.findByUserIdUsernameAndPublicAccessIn(anyString(), anyList()))
                .thenReturn(Collections.singletonList(collection1));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwner(newString("user:test"), requestDetails);
        assertEquals(1, bundle.getTotal());
        assertBaseValueSet((ValueSet) bundle.getEntryFirstRep().getResource(), URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(0, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
    }

    @Test
    public void testSearchValueSetByOwnerAndId_version_empty() {
        when(collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndOrganizationMnemonicOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList(), anyString()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwnerAndId(newString("org:OCL"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(0), URL_SOURCE_2, V_2_0, TM, TUMOR_DISORDER);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(1), URL_SOURCE_1, V_1_0, AD, ALLERGIC_DISORDER);
    }

    @Test
    public void testSearchValueSetByOwnerAndId_expression_owner_user() {
        List<CollectionsReference> references = newReferences(
                "/users/test/sources/source1/v1.0/concepts/"+AD+"/123/",
                "/users/test/sources/source2/v2.0/concepts/"+TUMOR_DISORDER+"/123/"
        );
        collection1.setCollectionsReferences(references);
        when(collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList(), anyString()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwnerAndId(newString("user:test"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(2, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(0), URL_SOURCE_2, V_2_0, TM, TUMOR_DISORDER);
        assertConceptComponent(0, valueSet.getCompose().getInclude().get(1), URL_SOURCE_1, V_1_0, AD, ALLERGIC_DISORDER);
    }

    @Test
    public void testSearchValueSetByOwnerAndId_expression_source_version_not_found() {
        List<CollectionsReference> references = newReferences(
                "/users/test/sources/source1/v1111.0/concepts/"+AD+"/123/",
                "/users/test/sources/source2/v2222.0/concepts/"+TUMOR_DISORDER+"/123/"
        );
        collection1.setCollectionsReferences(references);
        when(collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList(), anyString()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwnerAndId(newString("user:test"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(0, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
    }

    @Test
    public void testSearchValueSetByOwnerAndId_expression_source_not_found() {
        List<CollectionsReference> references = newReferences(
                "/users/test/sources/source111/v1.0/concepts/"+AD+"/123/",
                "/users/test/sources/source222/v2.0/concepts/"+TUMOR_DISORDER+"/123/"
        );
        collection1.setCollectionsReferences(references);
        when(collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList(), anyString()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwnerAndId(newString("user:test"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(0, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
    }

    @Test
    public void testSearchValueSetByOwnerAndId_expression_owner_not_found() {
        List<CollectionsReference> references = newReferences(
                "/users/OCL/sources/source111/v1.0/concepts/"+AD+"/123/",
                "/users/OCL/sources/source222/v2.0/concepts/"+TUMOR_DISORDER+"/123/"
        );
        collection1.setCollectionsReferences(references);
        when(collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList(), anyString()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList())).thenReturn(Collections.singletonList(cs22))
                .thenReturn(Collections.singletonList(cs11));
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwnerAndId(newString("user:test"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(0, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
    }

    @Test
    public void testSearchValueSetByOwnerAndId_expression_concept_not_found() {
        List<CollectionsReference> references = newReferences(
                "/users/OCL/sources/source1/v1.0/concepts/"+AD+"/123/",
                "/users/OCL/sources/source2/v2.0/concepts/"+TUMOR_DISORDER+"/123/"
        );
        collection1.setCollectionsReferences(references);
        when(collectionRepository.findFirstByMnemonicAndReleasedAndPublicAccessInAndUserIdUsernameOrderByCreatedAtDesc(anyString(), anyBoolean(), anyList(), anyString()))
                .thenReturn(collection1);
        when(sourceRepository.findFirstByMnemonicAndVersionAndUserIdUsernameAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source1).thenReturn(source2);
        ValueSetResourceProvider provider = valueSetProvider();
        Bundle bundle = provider.searchValueSetByOwnerAndId(newString("user:test"), newString("123"), null, null, requestDetails);
        assertEquals(1, bundle.getTotal());
        ValueSet valueSet = (ValueSet) bundle.getEntryFirstRep().getResource();
        assertBaseValueSet(valueSet, URL_COLLECTION_1, COLLECTION_1_NAME, COLLECTION_1_FULL_NAME, "Jon Doe 1", "jondoe1@gmail.com",
                "USA", TEST, COLLECTION_1_COPYRIGHT_TEXT);
        assertEquals(0, ((ValueSet) bundle.getEntryFirstRep().getResource()).getCompose().getInclude().size());
    }

    private void assertConceptComponent(int index, ValueSet.ConceptSetComponent component, String systemUrl, String systemVersion, String code, String display) {
        assertEquals(systemUrl, component.getSystem());
        assertEquals(systemVersion, component.getVersion());
        assertEquals(code, component.getConcept().get(index).getCode());
        assertEquals(display, component.getConcept().get(index).getDisplay());
    }

    private void assertBaseValueSet(ValueSet valueSet, String url, String name, String fullName,
                                    String contactName, String contactEmail, String jurisdictionCode, String purpose,
                                    String copyright) {
        assertEquals(url, valueSet.getUrl());
        assertEquals(name, valueSet.getName());
        assertEquals(fullName, valueSet.getTitle());
        assertEquals(contactName, valueSet.getContactFirstRep().getName());
        assertEquals(contactEmail, valueSet.getContactFirstRep().getTelecomFirstRep().getValue());
        assertEquals(jurisdictionCode, valueSet.getJurisdictionFirstRep().getCodingFirstRep().getCode());
        assertEquals(purpose, valueSet.getPurpose());
        assertEquals(copyright, valueSet.getCopyright());
    }

    @Test(expected = InvalidRequestException.class)
    public void testValidateCode_url_null() {
        validateCode(null, V_11_1, CS_URL, V_21_1, AD, null, null, null, OWNER_VAL);
    }

    @Test(expected = InvalidRequestException.class)
    public void testValidateCode_code_null() {
        validateCode(VS_URL, V_11_1, CS_URL, V_21_1, null, null, null, null, OWNER_VAL);
    }

    @Test(expected = InvalidRequestException.class)
    public void testValidateCode_system_null() {
        validateCode(VS_URL, V_11_1, null, V_21_1, AD, null, null, null, OWNER_VAL);
    }

    @Test(expected = InvalidRequestException.class)
    public void testValidateCode_code_coding() {
        validateCode(VS_URL, V_11_1, CS_URL, null, AD, null, null,
                new Coding("ABC", "ABC", "ABC"), OWNER_VAL);
    }

    @Test
    public void testValidateCode_systemversion_unmatch() {
        assertFalse(validateCode(VS_URL, V_11_1, CS_URL, V_21_1, AD, null, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_systemversion_match1() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, null, AD, null, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_systemversion_match2() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, null, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_match1() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, ALLERGIC_DISORDER, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_match2() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, TRASTORNO_ALERGICO, null, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_match1() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, ALLERGIC_DISORDER, EN, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_match2() {
        assertTrue(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, TRASTORNO_ALERGICO, ES, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_unmatch1() {
        assertFalse(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, ALLERGIC_DISORDER, ES, null, OWNER_VAL));
    }

    @Test
    public void testValidateCode_display_dl_unmatch2() {
        assertFalse(validateCode(VS_URL, V_11_1, CS_URL, V_21_2, AD, TRASTORNO_ALERGICO, EN, null, OWNER_VAL));
    }

    @Test
    public void testExpand() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        source1.setCanonicalUrl(CS_URL);
        source1.setMnemonic(CS);
        source2.setCanonicalUrl(CS_URL);
        source2.setMnemonic(CS);
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24), null, 0, 50, "");
        assertEquals(5, vs.getExpansion().getContains().size());
        assertContains(vs, 0, CS_URL, "v2.0", AD, ALLERGIC_DISORDER);
        assertContains(vs, 1, CS_URL, "v2.0", LUNG_PROCEDURE, LUNG_PROCEDURE_1);
        assertContains(vs, 2, CS_URL, "v2.0", TM, TUMOR_DISORDER);
        assertContains(vs, 3, CS_URL, "v2.0", VEIN_PROCEDURE, VEIN_PROCEDURE_1);
        assertContains(vs, 4, CS_URL, "v1.0", AD, ALLERGIC_DISORDER);
    }

    @Test
    public void testExpand_partial() {
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        source1.setCanonicalUrl(CS_URL);
        source1.setMnemonic(CS);
        source2.setCanonicalUrl(CS_URL);
        source2.setMnemonic(CS);
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs24, cs22, cs23), null, 2, 50, "");
        assertEquals(3, vs.getExpansion().getContains().size());
        assertContains(vs, 0, CS_URL, "v2.0", LUNG_PROCEDURE, LUNG_PROCEDURE_1);
        assertContains(vs, 1, CS_URL, "v2.0", TM, TUMOR_DISORDER);
        assertContains(vs, 2, CS_URL, "v2.0", VEIN_PROCEDURE, VEIN_PROCEDURE_1);
    }

    @Test
    public void testExpand_count_0() {
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Collections.singletonList(cs21), null,0, 0, "");
        assertEquals(5, vs.getExpansion().getTotal());
        assertEquals(0, vs.getExpansion().getContains().size());
    }

    @Test(expected = InvalidRequestException.class)
    public void testExpand_count_negative() {
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        runExpand(references, Collections.singletonList(cs11), Collections.singletonList(cs21), null, 0, -2, "");
    }

    @Test(expected = InvalidRequestException.class)
    public void testExpand_missing_url() {
        ValueSetResourceProvider provider = valueSetProvider();
        provider.valueSetExpand(null, null, new IntegerType(0), new IntegerType(10),
                null, null, null, null, null, null, null, newString(OWNER_VAL));
    }

    @Test(expected = InvalidRequestException.class)
    public void testExpand_unknown_systemversion() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24),
                null, 0, 50, CS_URL+"|unk");
    }

    @Test
    public void testExpand_known_systemversion() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24),
                Arrays.asList(cs31, cs32, cs33, cs34), 0, 50, CS_URL + "|v3.0");
        assertEquals(4, vs.getExpansion().getContains().size());
        assertContains(vs, 0, CS_URL, "v3.0", AD, ALLERGIC_DISORDER);
        assertContains(vs, 1, CS_URL, "v3.0", LUNG_PROCEDURE, LUNG_PROCEDURE_1);
        assertContains(vs, 2, CS_URL, "v3.0", TM, TUMOR_DISORDER);
        assertContains(vs, 3, CS_URL, "v3.0", VEIN_PROCEDURE, VEIN_PROCEDURE_1);
    }

    @Test
    public void testExpand_known_systemversion_no_head_in_expression() {
        // all match
        List<CollectionsReference> references = newReferences(
                "/orgs/OCL/sources/"+CS+"/v1.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+TM+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+VEIN_PROCEDURE+"/123/",
                "/orgs/OCL/sources/"+CS+"/v2.0/concepts/"+LUNG_PROCEDURE+"/123/"
        );
        ValueSet vs = runExpand(references, Collections.singletonList(cs11), Arrays.asList(cs21, cs22, cs23, cs24),
                Arrays.asList(cs31, cs32, cs33, cs34), 0, 50, CS_URL + "|v3.0");
        assertEquals(0, vs.getExpansion().getContains().size());
    }

    private void assertContains(ValueSet valueSet, int index, String system, String version, String code, String display) {
        assertEquals(system, valueSet.getExpansion().getContains().get(index).getSystem());
        assertEquals(version, valueSet.getExpansion().getContains().get(index).getVersion());
        assertEquals(code, valueSet.getExpansion().getContains().get(index).getCode());
        assertEquals(display, valueSet.getExpansion().getContains().get(index).getDisplay());
    }

    public ValueSet runExpand(List<CollectionsReference> references, List<ConceptsSource> list1, List<ConceptsSource> list2,
                              List<ConceptsSource> list3, Integer offset, Integer count, String systemVersion) {
        // set up
        ValueSetResourceProvider provider = valueSetProvider();
        Collection collection = collection(references);
        when(sourceRepository.findFirstByMnemonicAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(source2).thenReturn(source1);
        OngoingStubbing<List<ConceptsSource>> stub1 = when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(123L), anyList()));
        for (ConceptsSource cs1 : list1) {
            stub1 = stub1.thenReturn(Collections.singletonList(cs1));
        }
        OngoingStubbing<List<ConceptsSource>> stub2 = when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(234L), anyList()));
        for (ConceptsSource cs2 : list2) {
            stub2 = stub2.thenReturn(Collections.singletonList(cs2));
        }
        if (isValid(systemVersion) & !systemVersion.contains("unk")) {
            when(sourceRepository.findFirstByCanonicalUrlAndVersionAndPublicAccessIn(anyString(), anyString(), anyList())).thenReturn(source3);
            OngoingStubbing<List<ConceptsSource>> stub3 = when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(eq(345L), anyList()));
            if (list3 != null) {
                for (ConceptsSource cs3 : list3) {
                    stub3 = stub3.thenReturn(Collections.singletonList(cs3));
                }
            }
        }
        when(collectionRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                anyString(), anyBoolean(), anyString(), anyList())).thenReturn(collection);

        return provider.valueSetExpand(newUrl(VS_URL), null, new IntegerType(offset), new IntegerType(count),
                null, null, null, null, null, Sets.newHashSet(new CanonicalType(systemVersion)), null, newString(OWNER_VAL));
    }


    public Parameters validateCode(String url, String version, String system, String systemVersion, String code,
                                       String display, String language, Coding coding, String owner) {
        // set up
        ValueSetResourceProvider provider = valueSetProvider();
        Concept concept1 = concept1();
        Concept concept2 = concept2();

        Source source = source(123L, systemVersion, concept1, concept2);

        Collection collection = collection(newReferences(
                "/orgs/OCL/sources/"+CS+"/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/"+V_21_2+"/concepts/"+AD+"/123/",
                "/orgs/OCL/sources/"+CS+"/"+V_21_1+"/concepts/"+TM+"/123/"
        ));

        ConceptsSource cs1 = conceptsSource(concept1, source);
        ConceptsSource cs2 = conceptsSource(concept2, source);

        // mocks
        if (StringUtils.isNotBlank(version)) {
            when(collectionRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(),
                    anyString(), anyString(), anyList())).thenReturn(collection);
        } else {
            when(collectionRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                    anyString(), anyBoolean(), anyString(), anyList())).thenReturn(collection);
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            when(sourceRepository.findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(),
                    anyString(), anyString(), anyList())).thenReturn(source);
        } else {
            when(sourceRepository.findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(
                    anyString(), anyBoolean(), anyString(), anyList())).thenReturn(source);
        }

        when(conceptsSourceRepository.findBySourceIdAndConceptIdInOrderByConceptIdDesc(anyLong(), anyList()))
                .thenReturn(Arrays.asList(cs1, cs2));
        when(conceptRepository.findByMnemonic(anyString())).thenReturn(Arrays.asList(concept1, concept2));

        // call to test method
        Parameters output = provider.valueSetValidateCode(newUrl(url), newString(version), newCode(code), newUrl(system), newString(systemVersion),
                newString(display), newCode(language), coding, newString(owner));

        // verify
        if (StringUtils.isNotBlank(version)) {
            verify(collectionRepository, times(1))
                    .findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList());
        } else {
            verify(collectionRepository, times(1))
                    .findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyString(), anyList());
        }
        if (StringUtils.isNotBlank(systemVersion)) {
            verify(sourceRepository, times(1))
                    .findFirstByCanonicalUrlAndVersionAndOrganizationMnemonicAndPublicAccessIn(anyString(), anyString(), anyString(), anyList());
        } else {
            verify(sourceRepository, times(1))
                    .findFirstByCanonicalUrlAndReleasedAndOrganizationMnemonicAndPublicAccessInOrderByCreatedAtDesc(anyString(), anyBoolean(), anyString(), anyList());
        }
        return output;
    }
}