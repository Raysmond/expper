package com.expper.web.rest;

import com.expper.Application;
import com.expper.domain.Reply;
import com.expper.repository.ReplyRepository;
import com.expper.web.rest.dto.ReplyDTO;
import com.expper.web.rest.mapper.ReplyMapper;

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

import com.expper.domain.enumeration.ReplyStatus;

/**
 * Test class for the ReplyResource REST controller.
 *
 * @see AdminReplyResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ReplyResourceTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Z"));

    private static final String DEFAULT_CONTENT = "AAAAA";
    private static final String UPDATED_CONTENT = "BBBBB";


private static final ReplyStatus DEFAULT_STATUS = ReplyStatus.ACTIVE;
    private static final ReplyStatus UPDATED_STATUS = ReplyStatus.BLOCKED;

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_CREATED_AT_STR = dateTimeFormatter.format(DEFAULT_CREATED_AT);

    @Inject
    private ReplyRepository replyRepository;

    @Inject
    private ReplyMapper replyMapper;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restReplyMockMvc;

    private Reply reply;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AdminReplyResource replyResource = new AdminReplyResource();
        ReflectionTestUtils.setField(replyResource, "replyRepository", replyRepository);
        ReflectionTestUtils.setField(replyResource, "replyMapper", replyMapper);
        this.restReplyMockMvc = MockMvcBuilders.standaloneSetup(replyResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        reply = new Reply();
        reply.setContent(DEFAULT_CONTENT);
        reply.setStatus(DEFAULT_STATUS);
        reply.setCreatedAt(DEFAULT_CREATED_AT);
    }

    @Test
    @Transactional
    public void createReply() throws Exception {
        int databaseSizeBeforeCreate = replyRepository.findAll().size();

        // Create the Reply
        ReplyDTO replyDTO = replyMapper.replyToReplyDTO(reply);

        restReplyMockMvc.perform(post("/api/replys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(replyDTO)))
                .andExpect(status().isCreated());

        // Validate the Reply in the database
        List<Reply> replys = replyRepository.findAll();
        assertThat(replys).hasSize(databaseSizeBeforeCreate + 1);
        Reply testReply = replys.get(replys.size() - 1);
        assertThat(testReply.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testReply.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testReply.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
    }

    @Test
    @Transactional
    public void checkContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = replyRepository.findAll().size();
        // set the field null
        reply.setContent(null);

        // Create the Reply, which fails.
        ReplyDTO replyDTO = replyMapper.replyToReplyDTO(reply);

        restReplyMockMvc.perform(post("/api/replys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(replyDTO)))
                .andExpect(status().isBadRequest());

        List<Reply> replys = replyRepository.findAll();
        assertThat(replys).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = replyRepository.findAll().size();
        // set the field null
        reply.setStatus(null);

        // Create the Reply, which fails.
        ReplyDTO replyDTO = replyMapper.replyToReplyDTO(reply);

        restReplyMockMvc.perform(post("/api/replys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(replyDTO)))
                .andExpect(status().isBadRequest());

        List<Reply> replys = replyRepository.findAll();
        assertThat(replys).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreated_atIsRequired() throws Exception {
        int databaseSizeBeforeTest = replyRepository.findAll().size();
        // set the field null
        reply.setCreatedAt(null);

        // Create the Reply, which fails.
        ReplyDTO replyDTO = replyMapper.replyToReplyDTO(reply);

        restReplyMockMvc.perform(post("/api/replys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(replyDTO)))
                .andExpect(status().isBadRequest());

        List<Reply> replys = replyRepository.findAll();
        assertThat(replys).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllReplys() throws Exception {
        // Initialize the database
        replyRepository.saveAndFlush(reply);

        // Get all the replys
        restReplyMockMvc.perform(get("/api/replys"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(reply.getId().intValue())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].created_at").value(hasItem(DEFAULT_CREATED_AT_STR)));
    }

    @Test
    @Transactional
    public void getReply() throws Exception {
        // Initialize the database
        replyRepository.saveAndFlush(reply);

        // Get the reply
        restReplyMockMvc.perform(get("/api/replys/{id}", reply.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(reply.getId().intValue()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.created_at").value(DEFAULT_CREATED_AT_STR));
    }

    @Test
    @Transactional
    public void getNonExistingReply() throws Exception {
        // Get the reply
        restReplyMockMvc.perform(get("/api/replys/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReply() throws Exception {
        // Initialize the database
        replyRepository.saveAndFlush(reply);

		int databaseSizeBeforeUpdate = replyRepository.findAll().size();

        // Update the reply
        reply.setContent(UPDATED_CONTENT);
        reply.setStatus(UPDATED_STATUS);
        reply.setCreatedAt(UPDATED_CREATED_AT);
        ReplyDTO replyDTO = replyMapper.replyToReplyDTO(reply);

        restReplyMockMvc.perform(put("/api/replys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(replyDTO)))
                .andExpect(status().isOk());

        // Validate the Reply in the database
        List<Reply> replys = replyRepository.findAll();
        assertThat(replys).hasSize(databaseSizeBeforeUpdate);
        Reply testReply = replys.get(replys.size() - 1);
        assertThat(testReply.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testReply.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testReply.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    public void deleteReply() throws Exception {
        // Initialize the database
        replyRepository.saveAndFlush(reply);

		int databaseSizeBeforeDelete = replyRepository.findAll().size();

        // Get the reply
        restReplyMockMvc.perform(delete("/api/replys/{id}", reply.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Reply> replys = replyRepository.findAll();
        assertThat(replys).hasSize(databaseSizeBeforeDelete - 1);
    }
}
