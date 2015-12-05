package com.expper.web.rest;

import com.expper.Application;
import com.expper.domain.Content;
import com.expper.repository.ContentRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.expper.domain.enumeration.ContentStatus;

/**
 * Test class for the ContentResource REST controller.
 *
 * @see ContentResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ContentResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Z"));

    private static final String DEFAULT_TITLE = "AAAAA";
    private static final String UPDATED_TITLE = "BBBBB";
    private static final String DEFAULT_CONTENT = "AAAAA";
    private static final String UPDATED_CONTENT = "BBBBB";
    private static final String DEFAULT_SUMMARY = "AAAAA";
    private static final String UPDATED_SUMMARY = "BBBBB";


private static final ContentStatus DEFAULT_STATUS = ContentStatus.DRAFT;
    private static final ContentStatus UPDATED_STATUS = ContentStatus.PUBLISHED;
    private static final String DEFAULT_PERMALINK = "AAAAA";
    private static final String UPDATED_PERMALINK = "BBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_CREATED_AT_STR = dateTimeFormatter.format(DEFAULT_CREATED_AT);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_UPDATED_AT_STR = dateTimeFormatter.format(DEFAULT_UPDATED_AT);

    @Inject
    private ContentRepository contentRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restContentMockMvc;

    private Content content;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ContentResource contentResource = new ContentResource();
        ReflectionTestUtils.setField(contentResource, "contentRepository", contentRepository);
        this.restContentMockMvc = MockMvcBuilders.standaloneSetup(contentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        content = new Content();
        content.setTitle(DEFAULT_TITLE);
        content.setContent(DEFAULT_CONTENT);
        content.setSummary(DEFAULT_SUMMARY);
        content.setStatus(DEFAULT_STATUS);
        content.setPermalink(DEFAULT_PERMALINK);
        content.setCreatedAt(DEFAULT_CREATED_AT);
        content.setUpdatedAt(DEFAULT_UPDATED_AT);
    }

    @Test
    @Transactional
    public void createContent() throws Exception {
        int databaseSizeBeforeCreate = contentRepository.findAll().size();

        // Create the Content

        restContentMockMvc.perform(post("/api/contents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(content)))
                .andExpect(status().isCreated());

        // Validate the Content in the database
        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(databaseSizeBeforeCreate + 1);
        Content testContent = contents.get(contents.size() - 1);
        assertThat(testContent.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testContent.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testContent.getSummary()).isEqualTo(DEFAULT_SUMMARY);
        assertThat(testContent.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testContent.getPermalink()).isEqualTo(DEFAULT_PERMALINK);
        assertThat(testContent.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testContent.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = contentRepository.findAll().size();
        // set the field null
        content.setTitle(null);

        // Create the Content, which fails.

        restContentMockMvc.perform(post("/api/contents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(content)))
                .andExpect(status().isBadRequest());

        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = contentRepository.findAll().size();
        // set the field null
        content.setContent(null);

        // Create the Content, which fails.

        restContentMockMvc.perform(post("/api/contents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(content)))
                .andExpect(status().isBadRequest());

        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = contentRepository.findAll().size();
        // set the field null
        content.setStatus(null);

        // Create the Content, which fails.

        restContentMockMvc.perform(post("/api/contents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(content)))
                .andExpect(status().isBadRequest());

        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllContents() throws Exception {
        // Initialize the database
        contentRepository.saveAndFlush(content);

        // Get all the contents
        restContentMockMvc.perform(get("/api/contents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(content.getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
                .andExpect(jsonPath("$.[*].summary").value(hasItem(DEFAULT_SUMMARY.toString())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].permalink").value(hasItem(DEFAULT_PERMALINK.toString())))
                .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT_STR)))
                .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT_STR)));
    }

    @Test
    @Transactional
    public void getContent() throws Exception {
        // Initialize the database
        contentRepository.saveAndFlush(content);

        // Get the content
        restContentMockMvc.perform(get("/api/contents/{id}", content.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(content.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.summary").value(DEFAULT_SUMMARY.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.permalink").value(DEFAULT_PERMALINK.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT_STR))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT_STR));
    }

    @Test
    @Transactional
    public void getNonExistingContent() throws Exception {
        // Get the content
        restContentMockMvc.perform(get("/api/contents/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateContent() throws Exception {
        // Initialize the database
        contentRepository.saveAndFlush(content);

		int databaseSizeBeforeUpdate = contentRepository.findAll().size();

        // Update the content
        content.setTitle(UPDATED_TITLE);
        content.setContent(UPDATED_CONTENT);
        content.setSummary(UPDATED_SUMMARY);
        content.setStatus(UPDATED_STATUS);
        content.setPermalink(UPDATED_PERMALINK);
        content.setCreatedAt(UPDATED_CREATED_AT);
        content.setUpdatedAt(UPDATED_UPDATED_AT);

        restContentMockMvc.perform(put("/api/contents")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(content)))
                .andExpect(status().isOk());

        // Validate the Content in the database
        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(databaseSizeBeforeUpdate);
        Content testContent = contents.get(contents.size() - 1);
        assertThat(testContent.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testContent.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testContent.getSummary()).isEqualTo(UPDATED_SUMMARY);
        assertThat(testContent.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testContent.getPermalink()).isEqualTo(UPDATED_PERMALINK);
        assertThat(testContent.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testContent.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    public void deleteContent() throws Exception {
        // Initialize the database
        contentRepository.saveAndFlush(content);

		int databaseSizeBeforeDelete = contentRepository.findAll().size();

        // Get the content
        restContentMockMvc.perform(delete("/api/contents/{id}", content.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Content> contents = contentRepository.findAll();
        assertThat(contents).hasSize(databaseSizeBeforeDelete - 1);
    }
}
