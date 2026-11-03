package com.changzheng;

import com.changzheng.entity.User;
import com.changzheng.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
class ProfileIntegrationTest {
    private static final Path TEMP;
    static {
        try { TEMP = Files.createTempDirectory("changzheng-profile-test-"); }
        catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEMP.resolve("test.db"));
        registry.add("app.upload.directory", () -> TEMP.resolve("uploads").toString());
        registry.add("app.upload.public-base-url", () -> "https://example.test");
        registry.add("wechat.mock", () -> "true");
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserMapper users;
    private User user;

    @BeforeEach
    void createUser() {
        user = new User();
        user.setOpenid(UUID.randomUUID().toString());
        user.setNickname("原昵称");
        user.setCity("遵义");
        user.setProvince("贵州");
        user.setGender(1);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        users.insert(user);
    }

    @Test
    void avatarUploadSaveReadAndRankUsePersistedProfile() throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB), "png", out);
        var file = new MockMultipartFile("file", "../../avatar.png", "image/png", out.toByteArray());
        String response = mvc.perform(multipart("/home/avatar").file(file).param("userId", user.getId().toString()))
                .andExpect(jsonPath("$.code").value(0)).andReturn().getResponse().getContentAsString();
        String avatar = json.readTree(response).path("data").path("avatarUrl").asText();
        assertThat(avatar).startsWith("https://example.test/uploads/avatars/").endsWith(".png");
        String path = avatar.substring("https://example.test".length());
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(post("/home/saveUserInfo").contentType("application/json").content(json.writeValueAsString(
                Map.of("userId", user.getId(), "nickname", " 新昵称 ", "avatarUrl", avatar))))
                .andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.openid").doesNotExist());
        User stored = users.selectById(user.getId());
        assertThat(stored.getAvatarUrl()).isEqualTo(avatar);
        assertThat(stored.getCity()).isEqualTo("遵义");
        assertThat(stored.getProvince()).isEqualTo("贵州");
        assertThat(stored.getGender()).isEqualTo(1);
        mvc.perform(get("/home/userInfo/" + user.getId()))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.avatarUrl").value(avatar))
                .andExpect(jsonPath("$.data.openid").doesNotExist());
        for (String rank : java.util.List.of("getAllRank", "getTodayRank")) {
            String list = rank.equals("getAllRank") ? "allRank" : "todayRank";
            mvc.perform(get("/rank/" + rank).param("userId", user.getId().toString()))
                    .andExpect(jsonPath("$.data." + list + "[?(@.userId == " + user.getId() + ")].nickname")
                            .value(org.hamcrest.Matchers.hasItem("新昵称")))
                    .andExpect(jsonPath("$.data." + list + "[?(@.userId == " + user.getId() + ")].avatarUrl")
                            .value(org.hamcrest.Matchers.hasItem(avatar)));
        }
    }

    @Test
    void rejectsTemporaryPathsAndEmptyNicknameWithoutChangingDatabase() throws Exception {
        for (Map<String, Object> payload : java.util.List.of(
                Map.<String, Object>of("userId", user.getId(), "nickname", "  "),
                Map.<String, Object>of("userId", user.getId(), "avatarUrl", "wxfile://tmp"),
                Map.<String, Object>of("userId", user.getId(), "nickname", "长".repeat(33)))) {
            mvc.perform(post("/home/saveUserInfo").contentType("application/json").content(json.writeValueAsString(payload)))
                    .andExpect(jsonPath("$.code").value(40001));
        }
        assertThat(users.selectById(user.getId()).getNickname()).isEqualTo("原昵称");
    }

    @Test
    void rejectsFakeImagesAndOversizedFiles() throws Exception {
        for (MockMultipartFile file : java.util.List.of(
                new MockMultipartFile("file", "avatar.png", "image/png", "not a picture".getBytes()),
                new MockMultipartFile("file", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]))) {
            mvc.perform(multipart("/home/avatar").file(file).param("userId", user.getId().toString()))
                    .andExpect(jsonPath("$.code").value(40001));
        }
    }

    @Test
    void rejectsUnknownUserBeforeWritingAvatar() throws Exception {
        mvc.perform(multipart("/home/avatar").file(new MockMultipartFile("file", "x.png", "image/png", new byte[]{1}))
                .param("userId", "9223372036854775807")).andExpect(jsonPath("$.code").value(40002));
    }
}
